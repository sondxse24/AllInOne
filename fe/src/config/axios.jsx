import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (originalRequest.url && originalRequest.url.includes("/auth/logout")) {
      return Promise.reject(error);
    }

    if (originalRequest.url && originalRequest.url.includes("/auth/refresh")) {
      localStorage.removeItem("user");
      window.location.href = "/login";
      return Promise.reject(error);
    }

    if ((error.response?.status === 401 || error.response?.status === 403) && !originalRequest._retry) {
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
