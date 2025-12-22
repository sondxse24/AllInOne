import React, { useState, useEffect } from "react";
import { Modal, List, Avatar, Button, message } from "antd";
import { CheckOutlined, CloseOutlined } from "@ant-design/icons";
import api from "../../config/axios";

const FriendRequestsModal = ({ isOpen, onCancel, onRefreshFriends }) => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const res = await api.get("/friend/requests");
      setRequests(res.data.result || []);
    } catch (err) {
      console.error("Lỗi lấy lời mời", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) fetchRequests();
  }, [isOpen]);

  const handleResponse = async (friendshipId, status) => {
    try {
      // Gửi kèm id và status (ACCEPTED hoặc DECLINED)
      await api.put("/friend/accept", {
        id: friendshipId,
        status: status,
      });

      const successMsg = status === "ACCEPTED" ? "Đã chấp nhận lời mời kết bạn" : "Đã từ chối lời mời";

      message.success(successMsg);

      fetchRequests(); // Load lại danh sách sau khi xử lý
      if (status === "ACCEPTED") {
        onRefreshFriends(); // Chỉ load lại danh sách bạn bè nếu là ACCEPTED
      }
    } catch (err) {
      message.error("Thao tác không thành công");
    }
  };

  return (
    <Modal title="Lời mời kết bạn" open={isOpen} onCancel={onCancel} footer={null}>
      <List
        loading={loading}
        dataSource={requests}
        renderItem={(item) => (
          <List.Item
            actions={[
              <Button
                type="primary"
                shape="circle"
                icon={<CheckOutlined />}
                onClick={() => handleResponse(item.id, "ACCEPTED")}
                title="Chấp nhận"
              />,
              <Button
                danger
                shape="circle"
                icon={<CloseOutlined />}
                onClick={() => handleResponse(item.id, "DECLINED")}
                title="Từ chối"
              />,   
            ]}
          >
            <List.Item.Meta avatar={<Avatar src={item.avatar} />} title={item.username} description={item.email} />
          </List.Item>
        )}
      />
    </Modal>
  );
};

export default FriendRequestsModal;
