import React, { useState, useEffect, useRef } from "react";
import MainLayout from "../layouts/MainLayout";
import { Avatar, Input, Button, Modal, Form, Select, Typography, Empty, Badge, Spin } from "antd";
import {
  SendOutlined,
  UsergroupAddOutlined,
  UserAddOutlined,
  BellOutlined,
  VideoCameraOutlined,
  PhoneOutlined,
  UserOutlined,
} from "@ant-design/icons";
import api from "../config/axios";
import { useAuth } from "../context/AuthContext";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import "./ChatPage.css";

// Đảm bảo bạn đã update file VideoCall.jsx theo hướng dẫn ở câu trả lời trước để fix lỗi Repeat
import VideoCall from "../components/VideoCall";

import AddFriendModal from "./Friend/AddFriendModal";
import FriendRequestsModal from "./Friend/FriendRequestsModal";

const { Text } = Typography;

export default function ChatPage() {
  const { user } = useAuth();

  // --- STATE CƠ BẢN ---
  const [rooms, setRooms] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");
  const [loadingMessages, setLoadingMessages] = useState(false);

  // --- STATE MODAL ---
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAddFriendOpen, setIsAddFriendOpen] = useState(false);
  const [isRequestsOpen, setIsRequestsOpen] = useState(false);

  // --- STATE DATA ---
  const [friends, setFriends] = useState([]);
  const [requestCount, setRequestCount] = useState(0);

  const [form] = Form.useForm();

  // --- STATE VIDEO CALL ---
  const [callStatus, setCallStatus] = useState("IDLE"); // 'IDLE' | 'OUTGOING' | 'INCOMING' | 'JOINED'
  const [callRoomId, setCallRoomId] = useState(null);
  const [callerInfo, setCallerInfo] = useState(null); // { name, avatar }

  // --- REFS ---
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const subscriptionRef = useRef(null);

  // --- EFFECTS ---
  useEffect(() => {
    fetchRooms();
    fetchRequestCount();
    fetchFriends();
    connectWebSocket();
    return () => disconnectWebSocket();
  }, []);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  useEffect(() => {
    if (!selectedRoom) return;

    let isCurrentRoom = true;
    const loadRoomData = async () => {
      try {
        setLoadingMessages(true);
        setMessages([]);
        const res = await api.get(`/chat/messages/${selectedRoom.id}`);
        if (isCurrentRoom) {
          // Lọc bỏ các tin nhắn tín hiệu cũ nếu backend có lỡ lưu lại
          const cleanMessages = (res.data.result || []).filter((m) => !m.content?.startsWith("SIGNAL_CALL_"));
          setMessages(cleanMessages);
        }
      } catch (error) {
        console.error("Lỗi lấy lịch sử tin nhắn", error);
      } finally {
        if (isCurrentRoom) setLoadingMessages(false);
      }
    };

    loadRoomData();
    subscribeToRoom(selectedRoom.id);

    return () => {
      isCurrentRoom = false;
    };
  }, [selectedRoom]);

  // Xử lý khi đóng tab/reload
  useEffect(() => {
    const handleBeforeUnload = () => {
      if (callStatus === "JOINED" && stompClientRef.current && callRoomId) {
        stompClientRef.current.send(
          `/app/chat/${callRoomId}`,
          {},
          JSON.stringify({ senderId: user.userId, content: "SIGNAL_CALL_ENDED" })
        );
      }
    };
    window.addEventListener("beforeunload", handleBeforeUnload);
    return () => window.removeEventListener("beforeunload", handleBeforeUnload);
  }, [callStatus, callRoomId, user.userId]);

  // --- API CALLS ---
  const fetchRooms = async () => {
    try {
      const res = await api.get("/chat/my-rooms");
      setRooms(res.data.result);
    } catch (error) {
      console.error(error);
    }
  };

  const fetchFriends = async () => {
    try {
      const res = await api.get("/friend/list");
      setFriends(res.data.result);
    } catch (e) {
      console.log(e);
    }
  };

  const fetchRequestCount = async () => {
    try {
      const res = await api.get("/friend/requests");
      setRequestCount(res.data.result?.length || 0);
    } catch (e) {
      console.error(e);
    }
  };

  // --- HELPER FUNCTIONS ---
  const getPartnerInfo = (room) => {
    if (!room.participants || room.participants.length === 0) return null;
    return room.participants.find((p) => String(p.userId) !== String(user.userId));
  };

  const getDisplayName = (room) => {
    if (room.isGroup) return room.name || "Nhóm không tên";
    const partner = getPartnerInfo(room);
    return partner ? partner.userName : "Người dùng hệ thống";
  };

  const getDisplayAvatar = (room) => {
    if (room.isGroup) return null;
    const partner = getPartnerInfo(room);
    return partner ? partner.avatar : null;
  };

  // --- HANDLERS CHAT ---
  const handleCreateRoom = async (values) => {
    try {
      const payload = {
        name: values.members.length > 1 ? values.name : null,
        participantIds: [...(values.members || []), user.email],
      };
      await api.post("/chat/create", payload);
      setIsModalOpen(false);
      form.resetFields();
      fetchRooms();
    } catch (error) {
      console.error(error);
    }
  };

  const handleSelectFriend = async (friend) => {
    try {
      const payload = {
        name: null,
        participantIds: [user.email, friend.email],
      };
      const res = await api.post("/chat/create", payload);
      await fetchRooms();
      setSelectedRoom(res.data.result);
      setIsModalOpen(false);
    } catch (error) {
      console.error(error);
    }
  };

  const sendMessage = () => {
    if (inputMessage.trim() && stompClientRef.current && selectedRoom) {
      const chatMessage = {
        content: inputMessage,
        senderId: user.userId,
      };
      stompClientRef.current.send(`/app/chat/${selectedRoom.id}`, {}, JSON.stringify(chatMessage));
      setInputMessage("");
    }
  };

  // --- WEBSOCKET ---
  const connectWebSocket = () => {
    const socket = new SockJS("http://localhost:8080/ws");
    const client = Stomp.over(socket);
    // Tắt debug log của stomp để đỡ rối console
    client.debug = null;

    client.connect(
      {},
      () => {
        stompClientRef.current = client;
        if (selectedRoom) subscribeToRoom(selectedRoom.id);

        client.subscribe("/topic/public.status", (payload) => {
          const statusUpdate = JSON.parse(payload.body);
          setFriends((prevFriends) => {
            return prevFriends.map((friend) => {
              const socketId = statusUpdate.userId || statusUpdate.id;
              const isOnline = statusUpdate.online !== undefined ? statusUpdate.online : statusUpdate.isOnline;
              if (String(friend.id) === String(socketId)) {
                return { ...friend, isOnline: isOnline };
              }
              return friend;
            });
          });
        });
      },
      (err) => console.error("WebSocket error", err)
    );
  };

  const subscribeToRoom = (roomId) => {
    if (!stompClientRef.current?.connected) return;
    if (subscriptionRef.current) subscriptionRef.current.unsubscribe();

    subscriptionRef.current = stompClientRef.current.subscribe(`/topic/room/${roomId}`, (payload) => {
      const msg = JSON.parse(payload.body);
      const content = msg.content || "";

      // Debug: Xem tin nhắn đến là gì
      console.log("📩 SOCKET:", content);

      // --- LOGIC XỬ LÝ VIDEO CALL (QUAN TRỌNG) ---
      // Kiểm tra xem tin nhắn có phải là tín hiệu gọi không
      if (content.startsWith("SIGNAL_CALL_")) {
        // Chỉ xử lý Logic nếu tin nhắn đến từ người khác
        if (String(msg.senderId) !== String(user.userId)) {
          // 1. Nhận yêu cầu gọi
          if (content.startsWith("SIGNAL_CALL_REQUEST")) {
                const parts = content.split("::");
                setCallRoomId(roomId);
                setCallerInfo({ name: parts[1], avatar: parts[2] });
                setCallStatus("INCOMING");
          }

          // 2. Nhận chấp nhận
          else if (content.startsWith("SIGNAL_CALL_ACCEPTED")) {
            setCallStatus("JOINED");
          }

          // 3. Nhận từ chối
          else if (content === "SIGNAL_CALL_REJECTED") {
            setCallStatus("IDLE");
            setCallRoomId(null);
            Modal.info({ title: "Kết thúc", content: "Người nhận đã từ chối cuộc gọi." });
          }

          // 4. Nhận kết thúc
          else if (content === "SIGNAL_CALL_ENDED") {
            setCallStatus("IDLE");
            setCallRoomId(null);
            Modal.info({ title: "Thông báo", content: "Cuộc gọi đã kết thúc." });
          }
        }

        // QUAN TRỌNG: Return luôn để KHÔNG hiện tin nhắn tín hiệu này ra giao diện chat
        // (Dù là tin của mình hay của đối phương)
        return;
      }

      // --- TIN NHẮN THƯỜNG ---
      console.log("✅ Thêm tin nhắn vào list:", content); 
      setMessages((prev) => [...prev, msg]);
    });
  };

  const disconnectWebSocket = () => {
    if (stompClientRef.current) stompClientRef.current.disconnect();
  };

  // --- HANDLERS VIDEO CALL ---

  // 1. Người gọi bấm nút
  const handleStartVideoCall = () => {
    if (!selectedRoom || !stompClientRef.current) return;
    const roomId = selectedRoom.id;
    setCallRoomId(roomId);
    setCallStatus("OUTGOING");

    // Gửi tín hiệu gọi
    const callSignal = {
      senderId: user.userId,
      content: `SIGNAL_CALL_REQUEST::${user.username}::${user.avatar || ""}`,
    };
    stompClientRef.current.send(`/app/chat/${roomId}`, {}, JSON.stringify(callSignal));
  };

  // 2. Người nhận bấm trả lời
  const handleAnswerCall = () => {
    if (!stompClientRef.current) return;
    stompClientRef.current.send(
      `/app/chat/${callRoomId}`,
      {},
      JSON.stringify({
        senderId: user.userId,
        content: `SIGNAL_CALL_ACCEPTED::${callRoomId}`,
      })
    );
    setCallStatus("JOINED");
  };

  // 3. Từ chối / Hủy
  const handleRejectCall = () => {
    if (stompClientRef.current && callRoomId) {
      stompClientRef.current.send(
        `/app/chat/${callRoomId}`,
        {},
        JSON.stringify({
          senderId: user.userId,
          content: "SIGNAL_CALL_REJECTED",
        })
      );
    }
    setCallStatus("IDLE");
    setCallRoomId(null);
  };

  // --- RENDER ---
  return (
    <MainLayout>
      <div className="chat-container">
        {/* SIDEBAR */}
        <div className="chat-sidebar">
          <div
            className="sidebar-header"
            style={{
              padding: 15,
              borderBottom: "1px solid #eee",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <Text strong style={{ fontSize: 16 }}>
              Đoạn chat
            </Text>
            <div style={{ display: "flex", gap: "8px" }}>
              <Badge count={requestCount} size="small">
                <Button type="text" icon={<BellOutlined />} onClick={() => setIsRequestsOpen(true)} />
              </Badge>
              <Button type="text" icon={<UserAddOutlined />} onClick={() => setIsAddFriendOpen(true)} />
              <Button
                type="text"
                icon={<UsergroupAddOutlined />}
                onClick={() => {
                  fetchFriends();
                  setIsModalOpen(true);
                }}
              />
            </div>
          </div>
          <div style={{ overflowY: "auto", flex: 1 }}>
            {rooms.map((room) => (
              <div
                key={room.id}
                className={`room-item ${selectedRoom?.id === room.id ? "active" : ""}`}
                onClick={() => setSelectedRoom(room)}
              >
                <Avatar src={getDisplayAvatar(room)} style={{ backgroundColor: room.isGroup ? "#87d068" : "#1890ff" }}>
                  {getDisplayName(room)?.charAt(0)?.toUpperCase()}
                </Avatar>
                <div style={{ overflow: "hidden", marginLeft: 12, flex: 1 }}>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{getDisplayName(room)}</div>
                  <div style={{ fontSize: 12, color: "#888" }}>{room.isGroup ? "Nhóm chat" : "Tin nhắn riêng"}</div>
                </div>
              </div>
            ))}
            <div style={{ padding: "15px 15px 8px", borderTop: "1px solid #f0f0f0", marginTop: 10 }}>
              <Text type="secondary" style={{ fontSize: 12, fontWeight: "bold" }}>
                BẠN BÈ TRỰC TUYẾN ({friends.filter((f) => f.isOnline).length})
              </Text>
            </div>
            {friends.map((friend) => (
              <div key={friend.userId} className="room-item friend-item" onClick={() => handleSelectFriend(friend)}>
                <Badge dot status={friend.isOnline ? "success" : "default"} offset={[-5, 30]}>
                  <Avatar src={friend.avatar}>{friend.username?.charAt(0)}</Avatar>
                </Badge>
                <div style={{ marginLeft: 12 }}>
                  <div style={{ fontWeight: 500 }}>{friend.username}</div>
                  <div style={{ fontSize: 11, color: friend.isOnline ? "#52c41a" : "#ccc" }}>
                    {friend.isOnline ? "Đang hoạt động" : "Ngoại tuyến"}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* CHAT WINDOW */}
        <div className="chat-window">
          {selectedRoom ? (
            <>
              <div className="chat-header">
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <Avatar
                    src={getDisplayAvatar(selectedRoom)}
                    style={{ backgroundColor: selectedRoom.isGroup ? "#87d068" : "#1890ff" }}
                  >
                    {getDisplayName(selectedRoom)?.charAt(0)?.toUpperCase()}
                  </Avatar>
                  <div>
                    <Text strong style={{ fontSize: 16 }}>
                      {getDisplayName(selectedRoom)}
                    </Text>
                    <div style={{ fontSize: 12, color: "gray" }}>
                      {selectedRoom.isGroup ? `${selectedRoom.participants?.length || 0} thành viên` : "Đang hoạt động"}
                    </div>
                  </div>
                </div>
                <div>
                  <Button
                    type="text"
                    icon={<VideoCameraOutlined style={{ fontSize: "20px", color: "#1890ff" }} />}
                    onClick={handleStartVideoCall}
                    title="Gọi Video"
                  />
                </div>
              </div>

              <div className="message-list">
                {loadingMessages ? (
                  <div style={{ display: "flex", justifyContent: "center", marginTop: 20 }}>
                    <Spin />
                  </div>
                ) : (
                  messages.map((msg, index) => {
                    const isMe = String(msg.senderId) === String(user.userId);
                    return (
                      <div key={index} className={`message-wrapper ${isMe ? "me" : "other"}`}>
                        {!isMe && (
                          <Avatar src={msg.senderAvatar} size="small" style={{ marginRight: 8, marginTop: 4 }}>
                            {msg.senderName?.charAt(0)?.toUpperCase()}
                          </Avatar>
                        )}
                        <div
                          style={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: isMe ? "flex-end" : "flex-start",
                          }}
                        >
                          {!isMe && (
                            <div style={{ fontSize: 11, marginBottom: 2, marginLeft: 2, color: "#888" }}>
                              {msg.senderName || "Người lạ"}
                            </div>
                          )}
                          <div className="message-bubble" title={msg.timestamp}>
                            {msg.content}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              <div className="chat-input-area">
                <Input
                  size="large"
                  placeholder="Nhập tin nhắn..."
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onPressEnter={sendMessage}
                  style={{ borderRadius: 20 }}
                />
                <Button
                  type="primary"
                  shape="circle"
                  icon={<SendOutlined />}
                  size="large"
                  onClick={sendMessage}
                  style={{ marginLeft: 10 }}
                />
              </div>
            </>
          ) : (
            <div className="chat-empty-state">
              <Empty description="Chọn một đoạn chat hoặc bạn bè để bắt đầu" />
            </div>
          )}
        </div>
      </div>

      {/* --- MODALS --- */}
      <AddFriendModal isOpen={isAddFriendOpen} onCancel={() => setIsAddFriendOpen(false)} />
      <FriendRequestsModal
        isOpen={isRequestsOpen}
        onCancel={() => setIsRequestsOpen(false)}
        onRefreshFriends={fetchFriends}
      />
      <Modal title="Tạo cuộc trò chuyện mới" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null}>
        <Form form={form} onFinish={handleCreateRoom} layout="vertical">
          <Form.Item name="name" label="Tên nhóm">
            <Input placeholder="Ví dụ: Team Project A..." />
          </Form.Item>
          <Form.Item name="members" label="Chọn thành viên" rules={[{ required: true }]}>
            <Select mode="multiple">
              {friends.map((f) => (
                <Select.Option key={f.userId} value={f.email}>
                  {f.username}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            Bắt đầu
          </Button>
        </Form>
      </Modal>

      {/* --- VIDEO CALL MODALS --- */}
      <Modal
        title="Cuộc gọi đến..."
        open={callStatus === "INCOMING"}
        footer={null}
        closable={false}
        centered
        width={300}
      >
        <div style={{ textAlign: "center", paddingBottom: 20 }}>
          <Avatar size={80} src={callerInfo?.avatar} icon={<UserOutlined />} style={{ marginBottom: 15 }} />
          <h3 style={{ marginBottom: 30 }}>{callerInfo?.name || "Ai đó"} đang gọi...</h3>
          <div style={{ display: "flex", justifyContent: "center", gap: 30 }}>
            <Button
              shape="circle"
              size="large"
              danger
              icon={<PhoneOutlined rotate={135} />}
              style={{ width: 50, height: 50 }}
              onClick={handleRejectCall}
            />
            <Button
              type="primary"
              shape="circle"
              size="large"
              style={{ backgroundColor: "#52c41a", width: 50, height: 50 }}
              icon={<PhoneOutlined />}
              onClick={handleAnswerCall}
            />
          </div>
        </div>
      </Modal>

      <Modal open={callStatus === "OUTGOING"} footer={null} closable={false} centered width={300}>
        <div style={{ textAlign: "center", padding: 20 }}>
          <Spin size="large" />
          <p style={{ marginTop: 20, fontWeight: 500 }}>Đang kết nối...</p>
          <Button danger onClick={handleRejectCall} style={{ marginTop: 10 }}>
            Hủy cuộc gọi
          </Button>
        </div>
      </Modal>

      {/* 3. MÀN HÌNH VIDEO CHÍNH */}
      {callStatus === "JOINED" && callRoomId && (
        <VideoCall
          roomId={callRoomId}
          // SỬA Ở ĐÂY: Truyền props rời, không truyền object {{...}}
          userId={user.userId}
          userName={user.username || user.email}
          onLeave={() => {
            // Gửi tín hiệu kết thúc cho bên kia
            if (stompClientRef.current) {
              stompClientRef.current.send(
                `/app/chat/${callRoomId}`,
                {},
                JSON.stringify({ senderId: user.userId, content: "SIGNAL_CALL_ENDED" })
              );
            }
            setCallStatus("IDLE");
            setCallRoomId(null);
          }}
        />
      )}
    </MainLayout>
  );
}
