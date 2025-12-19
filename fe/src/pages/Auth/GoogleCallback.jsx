import React, { useEffect, useRef } from "react"; // Nhớ import useRef
import { useNavigate, useSearchParams } from "react-router-dom";
import { message, Spin } from "antd";
import { loginGoogle } from "../../services/auth"; // Hoặc auth-google tùy file bạn đặt
import { useAuth } from "../../context/AuthContext";

const GoogleCallback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const called = useRef(false); // 🛑 Biến cờ để chặn gọi 2 lần
  const { fetchUser } = useAuth(); // Lấy hàm này từ Context

  useEffect(() => {
    const code = searchParams.get("code");
    if (code && !called.current) {
      called.current = true;

      loginGoogle(code)
        .then(async () => {
          // CHỐT CHẶN: Đợi fetchUser lấy thông tin mới nhất xong rồi mới điều hướng
          await fetchUser();
          message.success("Đăng nhập thành công!");
          const redirectUrl = localStorage.getItem("redirectAfterLogin") || "/dashboard";
          navigate(redirectUrl);
        })
        .catch((err) => {
          // Chỉ về login nếu thực sự lỗi (không phải do request kép)
          if (err.response?.status !== 400) {
            navigate("/login");
          }
        });
    }
  }, []);

  return <Spin size="large" fullscreen tip="Đang kết nối Google..." />;
};

export default GoogleCallback;
