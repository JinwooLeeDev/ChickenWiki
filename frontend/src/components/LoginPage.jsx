import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "./Header";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, signup, isLoggedIn, user } = useAuth();
  const [mode, setMode] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [remember, setRemember] = useState(true);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const isSignup = mode === "signup";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const payload = { username, password, remember };
      if (isSignup) {
        await signup(payload);
      } else {
        await login(payload);
      }
      navigate("/", { replace: true });
    } catch (err) {
      setError(err.message || "\uB85C\uADF8\uC778 \uCC98\uB9AC\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 28, maxWidth: 720, margin: "0 auto" }}>
      <Header />
      <section
        style={{
          marginTop: 28,
          padding: 28,
          borderRadius: 18,
          background: "linear-gradient(180deg, #1f1f1f 0%, #151515 100%)",
          border: "1px solid #2a2a2a",
          boxShadow: "0 18px 36px rgba(0, 0, 0, 0.22)",
          textAlign: "left",
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", gap: 16, alignItems: "center" }}>
          <div>
            <h2 style={{ margin: 0 }}>{isSignup ? "\uD68C\uC6D0\uAC00\uC785" : "\uB85C\uADF8\uC778"}</h2>
            <p style={{ margin: "8px 0 0", color: "#9aa6b2" }}>
              {isSignup
                ? "\uB9AC\uBDF0\uB97C \uC791\uC131\uD560 \uACC4\uC815\uC744 \uB9CC\uB4E4\uC5B4\uBCF4\uC138\uC694."
                : "\uC544\uC774\uB514\uC640 \uBE44\uBC00\uBC88\uD638\uB85C ChickenWiki\uC5D0 \uB85C\uADF8\uC778\uD569\uB2C8\uB2E4."}
            </p>
          </div>
          {isLoggedIn ? (
            <div style={{ color: "#ffd700", fontWeight: 700 }}>
              {user.username}{"\uB2D8 \uB85C\uADF8\uC778 \uC911"}
            </div>
          ) : null}
        </div>

        <form onSubmit={handleSubmit} style={{ marginTop: 24 }}>
          <div style={{ marginBottom: 14 }}>
          <label>
            {"\uC544\uC774\uB514"}
            <input
              type="text"
              name="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              style={{
                width: "100%",
                boxSizing: "border-box",
                padding: "12px 14px",
                marginTop: 6,
                borderRadius: 12,
                border: "1px solid #333",
                background: "#101010",
                color: "white",
                outline: "none",
              }}
            />
          </label>
        </div>
          <div style={{ marginBottom: 14 }}>
          <label>
            {"\uBE44\uBC00\uBC88\uD638"}
            <input
              type="password"
              name="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete={isSignup ? "new-password" : "current-password"}
              style={{
                width: "100%",
                boxSizing: "border-box",
                padding: "12px 14px",
                marginTop: 6,
                borderRadius: 12,
                border: "1px solid #333",
                background: "#101010",
                color: "white",
                outline: "none",
              }}
            />
          </label>
        </div>

          <label style={{ display: "flex", gap: 8, alignItems: "center", color: "#c9d1d9", marginBottom: 16 }}>
            <input
              type="checkbox"
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
            />
            {"\uC774 \uBE0C\uB77C\uC6B0\uC800\uC5D0\uC11C \uB85C\uADF8\uC778 \uC720\uC9C0"}
          </label>

          {error ? (
            <div
              style={{
                padding: "10px 12px",
                borderRadius: 12,
                background: "rgba(255, 88, 88, 0.12)",
                color: "#ff8f8f",
                border: "1px solid rgba(255, 88, 88, 0.28)",
                marginBottom: 16,
              }}
            >
              {error}
            </div>
          ) : null}

          <button
            type="submit"
            disabled={loading}
            style={{
              width: "100%",
              padding: "12px 16px",
              borderRadius: 12,
              border: "none",
              background: "#ffd700",
              color: "#161616",
              fontWeight: 800,
              cursor: loading ? "not-allowed" : "pointer",
            }}
          >
            {loading
              ? "\uCC98\uB9AC \uC911..."
              : isSignup
                ? "\uD68C\uC6D0\uAC00\uC785\uD558\uACE0 \uB85C\uADF8\uC778"
                : "\uB85C\uADF8\uC778"}
          </button>
        </form>

        <button
          type="button"
          onClick={() => {
            setError("");
            setMode(isSignup ? "login" : "signup");
          }}
          style={{
            marginTop: 14,
            width: "100%",
            padding: "10px 16px",
            borderRadius: 12,
            border: "1px solid #333",
            background: "transparent",
            color: "#c9d1d9",
          }}
        >
          {isSignup
            ? "\uC774\uBBF8 \uACC4\uC815\uC774 \uC788\uC5B4\uC694. \uB85C\uADF8\uC778\uD560\uAC8C\uC694."
            : "\uCC98\uC74C\uC774\uC5D0\uC694. \uD68C\uC6D0\uAC00\uC785\uD560\uAC8C\uC694."}
        </button>
      </section>
    </div>
  );
}
