import React, { useState } from "react";
import { Form, Input, Button, Typography, message } from "antd";
import {
  GoogleOutlined,
  FacebookFilled,
  LinkedinFilled,
  UserOutlined,
  LockOutlined,
  MailOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";

import { login, register } from "../../services/auth";

import "./AuthPage.css";

const { Title, Text } = Typography;

const AuthPage = () => {
  const [isSignUp, setIsSignUp] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onFinishLogin = async (values) => {
    setLoading(true);
    try {
      const response = await login(values.email, values.password);

      message.success("Đăng nhập thành công!");
      navigate("/dashboard");
    } catch (err) {
      console.error(err);
      message.error("Đăng nhập thất bại! Kiểm tra lại email/password.");
    } finally {
      setLoading(false);
    }
  };

  const onFinishRegister = async (values) => {
    setLoading(true);
    try {
      await register(values.name, values.email, values.password);

      message.success("Đăng ký thành công! Hãy đăng nhập.");
      setIsSignUp(false);
    } catch (err) {
      console.error(err);
      message.error("Đăng ký thất bại!");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleRedirect = () => {
    const currentPath = window.location.pathname;
    if (currentPath !== "/login" && currentPath !== "/register") {
      localStorage.setItem("redirectAfterLogin", currentPath);
    }
    
    const CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    const REDIRECT_URI = import.meta.env.VITE_GOOGLE_REDIRECT_URI;

    const url = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&response_type=code&scope=email%20profile&access_type=offline&prompt=consent`;

    window.location.href = url;
  };

  return (
    <div className="auth-body">
      <div className={`container ${isSignUp ? "right-panel-active" : ""}`} id="container">
        <div className="form-container sign-up-container">
          <div className="form-content">
            <Title level={2} style={{ color: "#333" }}>
              Create Account
            </Title>
            <div className="social-container">
              <a href="#">
                <FacebookFilled />
              </a>
              <a onClick={handleGoogleRedirect} style={{ cursor: "pointer" }}>
                <GoogleOutlined />
              </a>
              <a href="#">
                <LinkedinFilled />
              </a>
            </div>
            <Text type="secondary" style={{ marginBottom: 15 }}>
              or use your email for registration
            </Text>

            <Form name="signup" onFinish={onFinishRegister} style={{ width: "100%" }} size="large">
              <Form.Item name="name" rules={[{ required: true, message: "Please input your name!" }]}>
                <Input prefix={<UserOutlined />} placeholder="Name" />
              </Form.Item>

              <Form.Item
                name="email"
                rules={[
                  { required: true, message: "Please input your email!" },
                  { type: "email", message: "Invalid email!" },
                ]}
              >
                <Input prefix={<MailOutlined />} placeholder="Email" />
              </Form.Item>

              <Form.Item name="password" rules={[{ required: true, message: "Please input your password!" }]}>
                <Input.Password prefix={<LockOutlined />} placeholder="Password" />
              </Form.Item>

              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                shape="round"
                style={{ backgroundColor: "#ff4b2b", borderColor: "#ff4b2b", width: "150px" }}
              >
                SIGN UP
              </Button>
            </Form>
          </div>
        </div>

        <div className="form-container sign-in-container">
          <div className="form-content">
            <Title level={2} style={{ color: "#333" }}>
              Sign in
            </Title>
            <div className="social-container">
              <a href="#">
                <FacebookFilled />
              </a>
              <a onClick={handleGoogleRedirect} style={{ cursor: "pointer" }}>
                <GoogleOutlined />
              </a>
              <a href="#">
                <LinkedinFilled />
              </a>
            </div>
            <Text type="secondary" style={{ marginBottom: 15 }}>
              or use your account
            </Text>

            <Form name="login" onFinish={onFinishLogin} style={{ width: "100%" }} size="large">
              <Form.Item
                name="email"
                rules={[
                  { required: true, message: "Please input your email!" },
                  { type: "email", message: "Invalid email!" },
                ]}
              >
                <Input prefix={<MailOutlined />} placeholder="Email" />
              </Form.Item>

              <Form.Item name="password" rules={[{ required: true, message: "Please input your password!" }]}>
                <Input.Password prefix={<LockOutlined />} placeholder="Password" />
              </Form.Item>

              <div style={{ marginBottom: 20 }}>
                <a href="#" style={{ color: "#333" }}>
                  Forgot your password?
                </a>
              </div>

              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                shape="round"
                style={{ backgroundColor: "#ff4b2b", borderColor: "#ff4b2b", width: "150px" }}
              >
                SIGN IN
              </Button>
            </Form>
          </div>
        </div>

        <div className="overlay-container">
          <div className="overlay">
            <div className="overlay-panel overlay-left">
              <Title level={2} style={{ color: "white" }}>
                Welcome Back!
              </Title>
              <p style={{ margin: "20px 0 30px" }}>To keep connected with us please login with your personal info</p>
              <Button className="ghost-btn" shape="round" size="large" onClick={() => setIsSignUp(false)}>
                SIGN IN
              </Button>
            </div>
            <div className="overlay-panel overlay-right">
              <Title level={2} style={{ color: "white" }}>
                Hello, Friend!
              </Title>
              <p style={{ margin: "20px 0 30px" }}>Enter your personal details and start journey with us</p>
              <Button className="ghost-btn" shape="round" size="large" onClick={() => setIsSignUp(true)}>
                SIGN UP
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
