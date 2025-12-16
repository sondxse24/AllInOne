import { useEffect, useState } from "react";
import api from "../config/axios";
import MainLayout from "../layouts/MainLayout";
import { Card, Descriptions, Spin, Tag } from "antd";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get("/users/me")
      .then((res) => setUser(res.data.result || res.data))
      .catch(() => setError("403 - Not authenticated"));
  }, []);

  return (
    <MainLayout>
      <h2>Dashboard</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {!user && !error ? <Spin tip="Loading..." /> : null}
      {user && (
        <Card title="Thông tin người dùng" bordered={false} style={{ maxWidth: 800 }}>
          <Descriptions bordered column={1}>
            <Descriptions.Item label="Username">{user.username}</Descriptions.Item>
            <Descriptions.Item label="Email">{user.email}</Descriptions.Item>
            <Descriptions.Item label="Role">
              <Tag color="blue">{user.role}</Tag>
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}
    </MainLayout>
  );
}
