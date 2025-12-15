import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/default/Login";
import Dashboard from "./pages/default/Dashboard";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}
