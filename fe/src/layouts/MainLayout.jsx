import React, { useState } from 'react';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { Layout, Menu, Button, theme, Avatar, Dropdown, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import api from '../config/axios'; // Import axios instance của bạn
import './MainLayout.css';

const { Header, Sider, Content } = Layout;

const MainLayout = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken();

  const user = JSON.parse(localStorage.getItem('user')) || { username: "Admin" };

  const handleLogout = async () => {
    try {
        await api.post('/auth/logout');
    } catch (error) {
        console.error("Logout error", error);
    } finally {
        message.success("Đăng xuất thành công!");
        navigate('/login');
    }
  };

  const userMenu = [
    {
      key: '1',
      label: 'Thông tin cá nhân',
      icon: <UserOutlined />,
    },
    {
      key: '2',
      label: 'Đăng xuất',
      icon: <LogoutOutlined />,
      onClick: handleLogout,
      danger: true,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div className="logo">
            {collapsed ? 'LabX' : 'LabX System'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          defaultSelectedKeys={['1']}
          onClick={({ key }) => {
            if (key === '1') navigate('/dashboard');
            if (key === '2') navigate('/users');
          }}
          items={[
            {
              key: '1',
              icon: <DashboardOutlined />,
              label: 'Dashboard',
            },
            {
              key: '2',
              icon: <UserOutlined />,
              label: 'Quản lý User',
            },
            {
              key: '3',
              icon: <SettingOutlined />,
              label: 'Cài đặt',
            },
          ]}
        />
      </Sider>

      <Layout>
        <Header style={{ padding: 0, background: colorBgContainer, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{
              fontSize: '16px',
              width: 64,
              height: 64,
            }}
          />

          <div style={{ marginRight: 24, display: 'flex', alignItems: 'center', gap: 10 }}>
            <span style={{ fontWeight: 600 }}>Hi, {user.username}</span>
            <Dropdown menu={{ items: userMenu }} placement="bottomRight">
                <Avatar size="large" icon={<UserOutlined />} style={{ cursor: 'pointer', backgroundColor: '#ff4b2b' }} />
            </Dropdown>
          </div>
        </Header>

        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}
        >
          {children} 
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;