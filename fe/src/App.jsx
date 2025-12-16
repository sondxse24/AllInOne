import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Auth/Login";
import Dashboard from "./pages/Dashboard";
import GoogleCallback from "./pages/Auth/GoogleCallback";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/oauth2/callback" element={<GoogleCallback />} />
      </Routes>
    </BrowserRouter>
  );
}
