import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import { Mail, Lock, LogIn } from "lucide-react";
import { useAuth } from "../hooks/useAuth.js";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "", role: "citizen" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const loggedInUser = await login(form.email, form.password, form.role);
      toast.success("Logged in");
      navigate(loggedInUser.role === "district_officer" ? "/admin" : "/dashboard");
    } catch (err) {
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="relative isolate flex min-h-[calc(100vh-4rem)] items-center justify-center overflow-hidden px-4 py-16">
      <div
        className="absolute inset-0 -z-20 bg-cover bg-center"
        style={{ backgroundImage: "url('https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=2200&q=75')" }}
        aria-hidden="true"
      />
      <div className="absolute inset-0 -z-10 bg-[#f4f7f9]/80" aria-hidden="true" />
      <div className="glass-panel w-full max-w-md p-7 sm:p-8">
        <h1 className="text-2xl font-bold text-[#102a43] mb-2">Login</h1>
        <p className="text-sm text-[#526579] mb-6">Sign in to report incidents and save locations.</p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm text-[#526579] block mb-1.5">Login as</label>
            <select
              className="input-field"
              value={form.role}
              onChange={(e) => setForm({ ...form, role: e.target.value })}
            >
              <option value="citizen">Citizen</option>
              <option value="district_officer">District authority</option>
            </select>
          </div>

          <div>
            <label className="text-sm text-[#526579] block mb-1.5">Email</label>
            <input
              type="email"
              required
              className="input-field"
              placeholder="you@example.com"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
            />
          </div>

          <div>
            <label className="text-sm text-[#526579] block mb-1.5">Password</label>
            <input
              type="password"
              required
              className="input-field"
              placeholder="••••••••"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
            />
          </div>

          {error && <p className="text-sm text-risk-extreme">{error}</p>}

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? "Logging in…" : "Login"}
          </button>
        </form>

        <p className="text-sm text-[#526579] text-center mt-6">
          Don't have an account?{" "}
          <Link to="/register" className="text-[#087f8c] hover:text-[#0b5266] font-medium">
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}
