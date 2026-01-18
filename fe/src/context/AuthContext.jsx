import React, { createContext, useState, useContext, useEffect, useMemo } from "react";
import api from "../config/axios";
import { Spin } from "antd";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchUser = async () => {
    try {
      const response = await api.get("/users/me");
      if (response.data?.result) {
        setUser(response.data.result);
      }
    } catch (error) {
      if (error.response?.status === 401) {
        setUser(null);
      }
      console.warn("Phiên làm việc chưa sẵn sàng");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const logout = () => {
    setUser(null);
    localStorage.removeItem("user");
  };

  const contextValue = useMemo(
    () => ({
      user,
      setUser,
      loading,
      logout,
      fetchUser,
    }),
    [user, loading]
  );

  return (
    <AuthContext.Provider value={contextValue}>
      {loading ? <Spin size="large" fullscreen tip="Đang tải dữ liệu..." /> : children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
