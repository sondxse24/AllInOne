import React, { useEffect, useRef } from 'react'; // Nhớ import useRef
import { useNavigate, useSearchParams } from 'react-router-dom';
import { message, Spin } from 'antd';
import { loginGoogle } from '../../services/auth'; // Hoặc auth-google tùy file bạn đặt

const GoogleCallback = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const called = useRef(false); // 🛑 Biến cờ để chặn gọi 2 lần

  useEffect(() => {
    const code = searchParams.get('code');
    
    // Chỉ chạy nếu có code VÀ chưa gọi lần nào
    if (code && !called.current) {
        called.current = true; // Đánh dấu là đã gọi
        
        loginGoogle(code)
            .then(() => {
                message.success('Đăng nhập Google thành công!');
                navigate('/dashboard');
            })
            .catch((err) => {
                console.error(err);
                message.error('Lỗi đăng nhập!');
                navigate('/login');
            });
    }
  }, []);

  return <Spin size="large" fullscreen tip="Đang kết nối Google..." />;
};

export default GoogleCallback;