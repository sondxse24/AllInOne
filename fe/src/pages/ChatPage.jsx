import React, { useState, useEffect, useRef } from "react";
import MainLayout from "../layouts/MainLayout";
import { List, Avatar, Input, Button, Modal, Form, Select, Typography, Spin, Empty } from "antd";
import { SendOutlined, PlusOutlined, UsergroupAddOutlined } from "@ant-design/icons";
import api from "../config/axios";
import { useAuth } from "../context/AuthContext";
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import "./ChatPage.css";

const { Text } = Typography;

export default function ChatPage() {
  const { user } = useAuth();

  // State quản lý dữ liệu
  const [rooms, setRooms] = useState([]); // Danh sách phòng
  const [selectedRoom, setSelectedRoom] = useState(null); // Phòng đang chọn
  const [messages, setMessages] = useState([]); // Tin nhắn của phòng hiện tại
  const [inputMessage, setInputMessage] = useState("");

  // State cho Modal tạo nhóm
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [allUsers, setAllUsers] = useState([]); // List user để chọn khi tạo nhóm
  const [form] = Form.useForm();

  // Ref WebSocket
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const subscriptionRef = useRef(null); // Để lưu subscription hiện tại (để unsubscribe khi đổi phòng)

  // 1. Load danh sách phòng khi vào trang
  useEffect(() => {
    fetchRooms();
    connectWebSocket();

    return () => disconnectWebSocket();
  }, []);

  // 2. Tự động cuộn xuống khi có tin nhắn mới
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // 3. Khi đổi phòng: Load lịch sử & Subscribe topic mới
  useEffect(() => {
    if (selectedRoom) {
      fetchHistory(selectedRoom.id);
      subscribeToRoom(selectedRoom.id);
    }
  }, [selectedRoom]);

  // --- CÁC HÀM API ---
  const fetchRooms = async () => {
    try {
      const res = await api.get("/chat/my-rooms");
      setRooms(res.data.result);
    } catch (error) {
      console.error("Lỗi lấy danh sách phòng", error);
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

  const fetchAllUsers = async () => {
    // Hàm này gọi API lấy danh sách toàn bộ user để add vào nhóm
    // Giả sử bạn có API /users
    try {
      const res = await api.get("/users/all");
      // Filter bỏ bản thân mình ra
      const otherUsers = res.data.result.filter((u) => u.username !== user.username);
      setAllUsers(otherUsers);
    } catch (e) {
      console.log(e);
    }
  };

  const handleCreateRoom = async (values) => {
    try {
      console.log("Members selected:", values.members);
      console.log("My email:", user?.email);

      const payload = {
        name: values.name,
        // Đảm bảo không gửi null vào mảng
        participantIds: [...(values.members || []), user.email].filter((item) => item !== null),
      };

      await api.post("/chat/create", payload);
      setIsModalOpen(false);
      form.resetFields();
      fetchRooms(); // Load lại danh sách
    } catch (error) {
      console.error("Lỗi tạo nhóm", error);
    }
  };

  // --- CÁC HÀM WEBSOCKET ---
  const connectWebSocket = () => {
    const socket = new SockJS("http://localhost:8080/ws");
    const client = Stomp.over(socket);
    client.debug = null;

    client.connect(
      {},
      () => {
        console.log("Connected to WebSocket");
        stompClientRef.current = client;
      },
      (err) => {
        console.error("WebSocket error", err);
      }
    );
  };

  const subscribeToRoom = (roomId) => {
    if (!stompClientRef.current || !stompClientRef.current.connected) return;

    // Nếu đã subscribe phòng khác rồi thì hủy đăng ký cũ đi
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
    }

    // Đăng ký kênh mới: /topic/room/{roomId}
    subscriptionRef.current = stompClientRef.current.subscribe(`/topic/room/${roomId}`, (payload) => {
      const newMessage = JSON.parse(payload.body);
      setMessages((prev) => [...prev, newMessage]);
    });
  };

  const sendMessage = () => {
    if (inputMessage.trim() && stompClientRef.current && selectedRoom) {
      const chatMessage = {
        content: inputMessage,
        senderId: user.username, // Hoặc ID tùy backend
      };

      // Gửi tới: /app/chat/{roomId}
      stompClientRef.current.send(`/app/chat/${selectedRoom.id}`, {}, JSON.stringify(chatMessage));
      setInputMessage("");
    }
  };

  const disconnectWebSocket = () => {
    if (stompClientRef.current) {
      stompClientRef.current.disconnect();
    }
  };

  // Mở modal tạo nhóm
  const showCreateModal = () => {
    fetchAllUsers();
    setIsModalOpen(true);
  };

  return (
    <MainLayout>
      <div className="chat-container">
        {/* --- CỘT TRÁI: DANH SÁCH PHÒNG --- */}
        <div className="chat-sidebar">
          <div
            style={{
              padding: 15,
              borderBottom: "1px solid #eee",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <Text strong>Đoạn chat</Text>
            <Button type="text" icon={<UsergroupAddOutlined />} onClick={showCreateModal} />
          </div>

          <div style={{ overflowY: "auto", flex: 1 }}>
            {rooms.map((room) => (
              <div
                key={room.id}
                className={`room-item ${selectedRoom?.id === room.id ? "active" : ""}`}
                onClick={() => setSelectedRoom(room)}
              >
                <Avatar style={{ backgroundColor: room.group ? "#87d068" : "#1890ff" }}>
                  {room.name ? room.name.charAt(0).toUpperCase() : "U"}
                </Avatar>
                <div style={{ overflow: "hidden" }}>
                  <div style={{ fontWeight: 600 }}>{room.name}</div>
                  <div
                    style={{
                      fontSize: 12,
                      color: "#888",
                      whiteSpace: "nowrap",
                      textOverflow: "ellipsis",
                      overflow: "hidden",
                    }}
                  >
                    {room.group ? "Nhóm chat" : "Tin nhắn riêng"}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* --- CỘT PHẢI: CỬA SỔ CHAT --- */}
        <div className="chat-window">
          {selectedRoom ? (
            <>
              {/* Header */}
              <div className="chat-header">
                <span>{selectedRoom.name}</span>
                <Button size="small">Info</Button>
              </div>

              {/* List Tin nhắn */}
              <div className="message-list">
                {messages.map((msg, index) => {
                  const isMe = msg.senderId === user.username; // So sánh username
                  return (
                    <div key={index} style={{ display: "flex", flexDirection: "column" }}>
                      <div className={`message-bubble ${isMe ? "me" : "other"}`}>
                        {!isMe && <span className="sender-name">{msg.senderId}</span>}
                        {msg.content}
                      </div>
                    </div>
                  );
                })}
                <div ref={messagesEndRef} />
              </div>

              {/* Input */}
              <div className="chat-input-area">
                <Input
                  placeholder="Nhập tin nhắn..."
                  value={inputMessage}
                  onChange={(e) => setInputMessage(e.target.value)}
                  onPressEnter={sendMessage}
                />
                <Button type="primary" icon={<SendOutlined />} onClick={sendMessage} />
              </div>
            </>
          ) : (
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                height: "100%",
                flexDirection: "column",
              }}
            >
              <Empty description="Chọn một đoạn chat để bắt đầu" />
            </div>
          )}
        </div>
      </div>

      {/* --- MODAL TẠO NHÓM --- */}
      <Modal title="Tạo cuộc trò chuyện mới" open={isModalOpen} onCancel={() => setIsModalOpen(false)} footer={null}>
        <Form form={form} onFinish={handleCreateRoom} layout="vertical">
          <Form.Item
            name="name"
            label="Tên nhóm (Để trống nếu chat riêng)"
            rules={[{ required: true, message: "Nhập tên nhóm!" }]}
          >
            <Input placeholder="Ví dụ: Team Building..." />
          </Form.Item>

          <Form.Item
            name="members"
            label="Thêm thành viên"
            rules={[{ required: true, message: "Chọn ít nhất 1 người!" }]}
          >
            <Select mode="multiple" placeholder="Chọn thành viên...">
              {allUsers.map((u) => (
                <Select.Option key={u.id} value={u.email}>
                  {u.username} ({u.email})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Button type="primary" htmlType="submit" block>
            Tạo nhóm
          </Button>
        </Form>
      </Modal>
    </MainLayout>
  );
}
