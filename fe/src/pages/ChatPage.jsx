import React, { useState, useEffect, useRef } from "react";
import MainLayout from "../layouts/MainLayout";
import { Avatar, Input, Button, Modal, Form, Select, Typography, Empty, Badge, Spin } from "antd";
import { SendOutlined, UsergroupAddOutlined, UserAddOutlined, BellOutlined } from "@ant-design/icons";
import api from "../config/axios";
import { useAuth } from "../context/AuthContext";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import "./ChatPage.css";

// Import các modal phụ (giữ nguyên nếu bạn đã có file)
import AddFriendModal from "./Friend/AddFriendModal";
import FriendRequestsModal from "./Friend/FriendRequestsModal";

const { Text } = Typography;

export default function ChatPage() {
  const { user } = useAuth(); // user cần chứa userId, email, username

  // --- STATE ---
  const [rooms, setRooms] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState("");

  const [loadingMessages, setLoadingMessages] = useState(false);

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAddFriendOpen, setIsAddFriendOpen] = useState(false);
  const [isRequestsOpen, setIsRequestsOpen] = useState(false);

  // Data State
  const [friends, setFriends] = useState([]);
  const [requestCount, setRequestCount] = useState(0);

  const [form] = Form.useForm();

  // Refs
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const subscriptionRef = useRef(null);

  // --- EFFECTS ---

  // 1. Init: Lấy phòng, bạn bè, connect socket
  useEffect(() => {
    fetchRooms();
    fetchRequestCount();
    connectWebSocket();
    return () => disconnectWebSocket();
  }, []);

  // 2. Scroll xuống cuối khi có tin nhắn mới
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // 3. Khi chọn phòng: Lấy lịch sử tin nhắn & Subscribe socket topic
  useEffect(() => {
    if (!selectedRoom) return;

    // Biến cờ: Đánh dấu đây là phòng hiện tại
    let isCurrentRoom = true;

    const loadRoomData = async () => {
      try {
        setLoadingMessages(true);
        // Reset tin nhắn ngay lập tức để không hiện tin nhắn của phòng cũ
        setMessages([]);

        // Gọi API lấy lịch sử
        const res = await api.get(`/chat/messages/${selectedRoom.id}`);

        // QUAN TRỌNG: Chỉ set data nếu user VẪN ĐANG ở phòng này
        // Nếu user đã switch sang phòng khác trong lúc chờ API, isCurrentRoom sẽ là false
        if (isCurrentRoom) {
          setMessages(res.data.result || []);
        }
      } catch (error) {
        console.error("Lỗi lấy lịch sử tin nhắn", error);
      } finally {
        if (isCurrentRoom) setLoadingMessages(false);
      }
    };

    // Thực thi load data
    loadRoomData();

    // Subscribe socket cho phòng mới
    subscribeToRoom(selectedRoom.id);

    // CLEANUP FUNCTION: Chạy khi user đổi sang phòng khác hoặc unmount
    return () => {
      isCurrentRoom = false; // Hủy cờ của phòng cũ
    };
  }, [selectedRoom]);

  // --- API CALLS ---

  const fetchRooms = async () => {
    try {
      const res = await api.get("/chat/my-rooms");
      // Backend trả về List<ChatRoomResponse>
      setRooms(res.data.result);
    } catch (error) {
      console.error("Lỗi lấy danh sách phòng", error);
    }
  };

  const fetchFriends = async () => {
    try {
      const res = await api.get("/friend/list");
      setFriends(res.data.result);
    } catch (e) {
      console.log("Lỗi lấy danh sách bạn bè", e);
    }
  };

  const fetchHistory = async (roomId) => {
    try {
      const res = await api.get(`/chat/messages/${roomId}`);
      setMessages(res.data.result);
    } catch (error) {
      console.error("Lỗi lấy lịch sử tin nhắn", error);
    }
  };

  const fetchRequestCount = async () => {
    try {
      const res = await api.get("/friend/requests");
      setRequestCount(res.data.result?.length || 0);
    } catch (e) {
      console.error("Lỗi đếm lời mời", e);
    }
  };

  // --- LOGIC XỬ LÝ (QUAN TRỌNG) ---

  // Helper: Tìm người đối diện trong chat 1-1 để lấy tên/avatar
  const getPartnerInfo = (room) => {
    if (!room.participants || room.participants.length === 0) return null;
    // Tìm người có userId KHÁC với userId của mình
    // Lưu ý ép kiểu String để so sánh chính xác
    return room.participants.find((p) => String(p.userId) !== String(user.userId));
  };

  // Helper: Lấy tên hiển thị
  const getDisplayName = (room) => {
    if (room.isGroup) {
      return room.name || "Nhóm không tên";
    }
    const partner = getPartnerInfo(room);
    return partner ? partner.userName : "Người dùng hệ thống";
  };

  // Helper: Lấy Avatar hiển thị
  const getDisplayAvatar = (room) => {
    if (room.isGroup) return null; // Để null để Antd hiển thị mặc định hoặc icon nhóm
    const partner = getPartnerInfo(room);
    return partner ? partner.avatar : null;
  };

  // --- HANDLERS ---

  const handleCreateRoom = async (values) => {
    try {
      // Backend check: nếu list size > 2 -> isGroup = true
      // values.members là mảng các email
      const payload = {
        name: values.members.length > 1 ? values.name : null,
        participantIds: [...(values.members || []), user.email],
      };

      await api.post("/chat/create", payload);

      setIsModalOpen(false);
      form.resetFields();
      fetchRooms(); // Load lại list để thấy phòng mới
    } catch (error) {
      console.error("Lỗi tạo nhóm", error);
    }
  };

  const handleSelectFriend = async (friend) => {
    try {
      const payload = {
        name: null,
        participantIds: [user.email, friend.email], // Gửi email để tìm user
      };

      const res = await api.post("/chat/create", payload);
      const targetRoom = res.data.result;

      // Refresh list và chọn phòng vừa tạo/tìm thấy
      await fetchRooms();
      setSelectedRoom(targetRoom);

      // Đóng modal friend list nếu đang mở (nếu bạn dùng logic đó)
      setIsModalOpen(false);
    } catch (error) {
      console.error("Lỗi khi mở chat với bạn bè", error);
    }
  };

  const sendMessage = () => {
    if (inputMessage.trim() && stompClientRef.current && selectedRoom) {
      const chatMessage = {
        content: inputMessage,
        senderId: user.userId, // Hoặc user.userId tùy backend lưu gì
      };
      stompClientRef.current.send(`/app/chat/${selectedRoom.id}`, {}, JSON.stringify(chatMessage));
      setInputMessage("");
    }
  };

  // --- WEBSOCKET ---

  const connectWebSocket = () => {
    const socket = new SockJS("http://localhost:8080/ws");
    const client = Stomp.over(socket);
    // client.debug = null; // Bật lại nếu muốn xem log socket
    client.connect(
      {},
      () => {
        stompClientRef.current = client;
        // Nếu đang chọn phòng thì sub lại ngay
        if (selectedRoom) subscribeToRoom(selectedRoom.id);
      },
      (err) => console.error("WebSocket error", err)
    );
  };

  const subscribeToRoom = (roomId) => {
    if (!stompClientRef.current?.connected) return;

    // Unsub topic cũ để tránh nhận tin nhắn đúp/sai phòng
    if (subscriptionRef.current) subscriptionRef.current.unsubscribe();

    subscriptionRef.current = stompClientRef.current.subscribe(`/topic/room/${roomId}`, (payload) => {
      const newMessage = JSON.parse(payload.body);
      setMessages((prev) => [...prev, newMessage]);
    });
  };

  const disconnectWebSocket = () => {
    if (stompClientRef.current) stompClientRef.current.disconnect();
  };

  return (
    <MainLayout>
      <div className="chat-container">
        {/* === SIDEBAR === */}
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
              <Badge count={requestCount} size="small" offset={[-2, 5]}>
                <Button type="text" icon={<BellOutlined />} onClick={() => setIsRequestsOpen(true)} />
              </Badge>
              <Button
                type="text"
                icon={<UserAddOutlined />}
                onClick={() => setIsAddFriendOpen(true)}
                title="Thêm bạn bằng Email"
              />
              <Button
                type="text"
                icon={<UsergroupAddOutlined />}
                onClick={() => {
                  fetchFriends();
                  setIsModalOpen(true);
                }}
                title="Tạo nhóm chat mới"
              />
            </div>
          </div>

          <div style={{ overflowY: "auto", flex: 1 }}>
            {/* DANH SÁCH PHÒNG CHAT */}
            {rooms.length > 0 ? (
              rooms.map((room) => (
                <div
                  key={room.id}
                  className={`room-item ${selectedRoom?.id === room.id ? "active" : ""}`}
                  onClick={() => setSelectedRoom(room)}
                >
                  <Avatar
                    src={getDisplayAvatar(room)}
                    style={{ backgroundColor: room.isGroup ? "#87d068" : "#1890ff" }}
                  >
                    {/* Nếu không có avatar thì hiện chữ cái đầu */}
                    {getDisplayName(room)?.charAt(0)?.toUpperCase()}
                  </Avatar>

                  <div style={{ overflow: "hidden", marginLeft: 12, flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{getDisplayName(room)}</div>
                    <div style={{ fontSize: 12, color: "#888" }}>{room.isGroup ? "Nhóm chat" : "Tin nhắn riêng"}</div>
                  </div>
                </div>
              ))
            ) : (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="Chưa có tin nhắn nào"
                style={{ marginTop: 20 }}
              />
            )}

            {/* DANH SÁCH BẠN BÈ (ĐỂ CHAT NHANH) */}
            <div style={{ padding: "15px 15px 8px", borderTop: "1px solid #f0f0f0", marginTop: 10 }}>
              <Text type="secondary" style={{ fontSize: 12, fontWeight: "bold" }}>
                BẠN BÈ TRỰC TUYẾN ({friends.length})
              </Text>
            </div>
            {friends.map((friend) => (
              <div key={friend.userId} className="room-item friend-item" onClick={() => handleSelectFriend(friend)}>
                <Badge dot status="success" offset={[-5, 30]}>
                  <Avatar src={friend.avatar}>{friend.username?.charAt(0)}</Avatar>
                </Badge>
                <div style={{ marginLeft: 12 }}>
                  <div style={{ fontWeight: 500 }}>{friend.username}</div>
                  <div style={{ fontSize: 11, color: "#52c41a" }}>Nhấn để chat</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* === CHAT WINDOW === */}
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

      {/* Modal Tạo Nhóm */}
      <Modal title="Tạo cuộc trò chuyện mới" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null}>
        <Form form={form} onFinish={handleCreateRoom} layout="vertical">
          <Form.Item name="name" label="Tên nhóm (Không bắt buộc)" help="Chỉ hiển thị khi có trên 2 thành viên">
            <Input placeholder="Ví dụ: Team Project A..." />
          </Form.Item>

          <Form.Item
            name="members"
            label="Chọn thành viên"
            rules={[{ required: true, message: "Vui lòng chọn ít nhất 1 người!" }]}
          >
            <Select mode="multiple" placeholder="Tìm kiếm bạn bè..." optionFilterProp="children">
              {friends.map((f) => (
                <Select.Option key={f.userId} value={f.email}>
                  {f.username} ({f.email})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Button type="primary" htmlType="submit" block size="large">
            Bắt đầu trò chuyện
          </Button>
        </Form>
      </Modal>
    </MainLayout>
  );
}
