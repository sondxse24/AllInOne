import axios from "axios";

// 1. Hàm helper để đọc cookie (Vì document.cookie là chuỗi dài ngoằng)
function getCookie(name) {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop().split(";").shift();
  return null;
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // BẮT BUỘC để gửi cookie đi
});

// --- REQUEST INTERCEPTOR ---
api.interceptors.request.use(
  (config) => {
    // Với Cookie HttpOnly, ta KHÔNG CẦN gửi Header Authorization (Bearer...)
    // Trình duyệt sẽ tự lo việc đó.

    // NHƯNG: Ta BẮT BUỘC phải gửi X-XSRF-TOKEN đối với các request thay đổi dữ liệu (POST, PUT, DELETE)
    if (config.method !== "get") {
      const xsrfToken = getCookie("XSRF-TOKEN");
      if (xsrfToken) {
        // Spring Security mặc định check header tên là 'X-XSRF-TOKEN'
        config.headers["X-XSRF-TOKEN"] = xsrfToken;
      }
    }

    // (Optional) Log để kiểm tra xem đã gắn được chưa
    // console.log("Header sent:", config.headers);

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// --- RESPONSE INTERCEPTOR (Giữ nguyên logic cũ của bạn) ---
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Vẫn giữ logic refresh token khi gặp lỗi 401
    // Lưu ý: Backend trả 401 thì mới refresh, 403 thì thôi.
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        await api.post("/auth/refresh");
        return api(originalRequest);
      } catch (refreshError) {
        // Xử lý logout nếu refresh thất bại...
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default api;
