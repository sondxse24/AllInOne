import React, { useState } from "react";
import { Modal, Input, List, Avatar, Button, message } from "antd";
import { UserAddOutlined, SearchOutlined } from "@ant-design/icons";
import api from "../../config/axios";

const AddFriendModal = ({ isOpen, onCancel }) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);

  // 1. Tìm kiếm người dùng (API này bạn có thể dùng /api/users/search?q=...)
  const handleSearch = async () => {
    if (!searchQuery) return;
    setLoading(true);
    try {
      const res = await api.get(`/users/username?username=${searchQuery}`);
      setResults(res.data.result);
    } catch (err) {
      message.error("Không tìm thấy người dùng");
    } finally {
      setLoading(false);
    }
  };

  // 2. Gửi lời mời kết bạn (Gọi API /api/friend/add)
  const handleAddFriend = async (targetId) => {
    console.log("Đang gửi kết bạn tới ID:", targetId); // Debug dòng này
    if (!targetId) {
      message.error("Lỗi: Không tìm thấy ID người dùng!");
      return;
    }
    try {
      await api.post("/friend/add", {
        addresseeId: targetId, // Phải khớp với tên trường trong AddFriendRequest của bạn
      });
      message.success("Đã gửi lời mời kết bạn!");
    } catch (err) {
      message.error("Lỗi: " + (err.response?.data?.message || "Không thể gửi lời mời"));
    }
  };

  return (
    <Modal title="Thêm bạn mới" open={isOpen} onCancel={onCancel} footer={null}>
      <Input.Search
        placeholder="Nhập email hoặc username..."
        enterButton={<SearchOutlined />}
        size="large"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        onSearch={handleSearch}
        loading={loading}
      />

      <List
        style={{ marginTop: 20 }}
        dataSource={results}
        renderItem={(item) => (
          <List.Item
            actions={[
              <Button type="primary" icon={<UserAddOutlined />} onClick={() => handleAddFriend(item.userId)}>
                Kết bạn
              </Button>,
            ]}
          >
            <List.Item.Meta avatar={<Avatar src={item.avatar} />} title={item.username} description={item.email} />
          </List.Item>
        )}
      />
    </Modal>
  );
};

export default AddFriendModal;
