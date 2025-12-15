import { useEffect, useState } from "react";
import api from "../../config/axios";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get("/users/me")
      .then(res => setUser(res.data))
      .catch(() => setError("403 - Not authenticated"));
  }, []);

  return (
    <div>
      <h2>Dashboard</h2>
      {user && <pre>{JSON.stringify(user, null, 2)}</pre>}
      {error && <p style={{ color: "red" }}>{error}</p>}
    </div>
  );
}
