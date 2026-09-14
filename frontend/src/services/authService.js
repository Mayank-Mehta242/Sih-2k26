import { api } from "./api.js";

export const authService = {
  async login(email, password, role) {
    const { data } = await api.post("/auth/login", { email, password, role });
    return data;
  },

  async register(payload) {
    const { data } = await api.post("/auth/register", payload);
    return data;
  },

  async getCurrentUser(token) {
    const { data } = await api.get("/auth/me");
    return data.user;
  },
};
