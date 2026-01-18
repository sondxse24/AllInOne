import api from "../config/axios";

export const login = (email, password) => {
  return api.post("/auth/login", {
    email,
    password,
  });
};

export const register = (name, email, password) => {
  return api.post("/users/create", {
    name,
    email,
    password,
  });
};

export const loginGoogle = (code) => {
  return api.post("/auth/google", { code });
};