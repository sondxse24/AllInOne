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

    // --- DEBUG LOG (Xem nó in ra số mấy) ---
    console.log("❌ Lỗi API:", error.response?.status, error.config.url);

    // Sửa điều kiện: Bắt cả 401 VÀ 403
    if (
      (error.response?.status === 401 || error.response?.status === 403) && 
      !originalRequest._retry
    ) {
      console.log("--> Phát hiện lỗi Auth, đang thử Refresh..."); // Nếu thấy dòng này là ngon
      originalRequest._retry = true;

      try {
        await api.post("/auth/refresh");
        console.log("--> Refresh thành công! Gọi lại API cũ...");
        return api(originalRequest);
        
      } catch (refreshError) {
        console.error("--> Refresh thất bại:", refreshError);
        localStorage.removeItem("user");
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;