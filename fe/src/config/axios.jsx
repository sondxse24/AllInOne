import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // 🛑 QUAN TRỌNG: CHẶN ĐỨNG SPAM
    // Nếu cái API đang bị lỗi chính là '/auth/refresh' -> Thì có nghĩa là hết cứu -> Logout luôn
    if (originalRequest.url && originalRequest.url.includes('/auth/refresh')) {
        localStorage.removeItem("user");
        window.location.href = "/login";
        return Promise.reject(error);
    }

    if (
      (error.response?.status === 401 || error.response?.status === 403) && 
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        // Gọi API làm mới token
        await api.post("/auth/refresh");
        
        // Refresh thành công -> Gọi lại request cũ
        return api(originalRequest);
        
      } catch (refreshError) {
        // Refresh thất bại -> Logout
        localStorage.removeItem("user");
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;