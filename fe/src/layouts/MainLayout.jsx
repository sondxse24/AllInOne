import React, { useState } from "react";
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from "@ant-design/icons";
import { Layout, Menu, Button, theme, Avatar, Dropdown, message, Tag, Spin } from "antd";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../config/axios";
import { useAuth } from "../context/AuthContext";
import "./MainLayout.css";

const { Header, Sider, Content } = Layout;

const MainLayout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuth();
  
  // Mặc định loading là false vì AuthContext đã lo việc này
  // Nếu muốn kỹ thì lấy loading từ AuthContext
  const { loading } = useAuth(); 

  const navigate = useNavigate();
  const location = useLocation();
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const handleLogout = async () => {
    try {
      await api.post("/auth/logout");
    } catch (err) {
      console.error("Logout error", err);
    } finally {
      logout();
      message.success("Đăng xuất thành công!");
      navigate("/login");
    }
  };

  const userMenu = {
    items: [
      { key: "1", label: "Thông tin cá nhân", icon: <UserOutlined /> },
      { key: "2", label: "Đăng xuất", icon: <LogoutOutlined />, onClick: handleLogout, danger: true },
    ],
  };

  const getSelectedKey = () => {
    if (location.pathname.startsWith("/dashboard")) return ["1"];
    if (location.pathname.startsWith("/users")) return ["2"];
    return ["1"];
  };

  return (
    // 1. Thêm hasSider để báo cho Antd biết đây là layout ngang
    <Layout style={{ minHeight: "100vh" }} hasSider> 
      
      {/* 2. Sider cố định bên trái */}
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed} 
        className="custom-sider" 
        width={240}
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          zIndex: 100 // Đè lên mọi thứ khác
        }}
      >
        <div className="logo-container">
          <div className="logo">{collapsed ? "V" : "Cloud"}</div>
        </div>

        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKey()}
          onClick={({ key }) => {
            if (key === "1") navigate("/dashboard");
            if (key === "2") navigate("/users");
          }}
          items={[
            { key: "1", icon: <DashboardOutlined />, label: "Dashboard" },
            { key: "2", icon: <UserOutlined />, label: "Quản lý User" },
            { key: "3", icon: <SettingOutlined />, label: "Cài đặt" },
          ]}
          style={{ padding: "0 8px" }}
        />
      </Sider>

      {/* 3. Layout con nằm bên phải, margin-left bằng width của Sider */}
      <Layout 
        style={{ 
            marginLeft: collapsed ? 80 : 240, // Tự động co giãn theo Sider
            transition: 'all 0.2s', // Hiệu ứng mượt mà khi co giãn
            minHeight: '100vh'
        }}
      >
        <Header
          className="site-header"
          style={{
            padding: 0,
            background: colorBgContainer,
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            position: 'sticky', // Dính header lên trên cùng khi cuộn
            top: 0,
            zIndex: 99,
            width: '100%'
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: "16px", width: 64, height: 64 }}
          />

          <div style={{ marginRight: 24, display: "flex", alignItems: "center", gap: 12 }}>
            {loading ? (
              <Spin size="small" />
            ) : user ? (
              <>
                <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", lineHeight: "1.2" }}>
                  <span style={{ fontWeight: 600, fontSize: "14px" }}>Hi, {user.username}</span>
                  {user.role && (
                    <Tag color="geekblue" style={{ margin: "4px 0 0 0", fontSize: "10px", lineHeight: "16px" }}>
                      {user.role}
                    </Tag>
                  )}
                </div>
                <Dropdown menu={userMenu} placement="bottomRight" arrow>
                  <Avatar
                    size="large"
                    src={user.avatar}
                    icon={!user.avatar && <UserOutlined />}
                    style={{ cursor: "pointer", backgroundColor: "#ff4b2b", border: "1px solid #d9d9d9" }}
                  />
                </Dropdown>
              </>
            ) : (
              <Button type="primary" onClick={() => navigate("/login")}>
                Login
              </Button>
            )}
          </div>
        </Header>

        <Content
          style={{
            margin: "24px 16px",
            padding: 24,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            boxShadow: "0 0 10px rgba(0,0,0,0.05)",
            overflow: 'initial' // Để nội dung dài vẫn hiện đẹp
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;