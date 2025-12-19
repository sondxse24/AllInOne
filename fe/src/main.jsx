import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import './index.css';
import { AuthProvider } from './context/AuthContext';

ReactDOM.createRoot(document.getElementById('root')).render(
  // ❌ BỎ <React.StrictMode> ĐI
  <AuthProvider>
    <App />
  </AuthProvider>
  // ❌ BỎ </React.StrictMode> ĐI
);