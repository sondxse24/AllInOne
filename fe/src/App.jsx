import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Auth/Login";
import Dashboard from "./pages/Dashboard";
import GoogleCallback from "./pages/Auth/GoogleCallback";
import ProtectedRoute from "./routing/ProtectedRoute";
import ChatPage from "./pages/ChatPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route element={<ProtectedRoute />}>
           <Route path="/dashboard" element={<Dashboard />} />
           <Route path="/chat" element={<ChatPage />} /> 
        </Route>

        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/oauth2/callback" element={<GoogleCallback />} />
      </Routes>
    </BrowserRouter>
  );
}
