import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login, signup } from "../services/api";

export default function LoginPage() {
  const navigate = useNavigate();
  const [mode, setMode] = useState("login");
  const [username, setUsername] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isSignup = mode === "signup";

  const resetMessages = () => {
    setError("");
    setSuccessMessage("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    resetMessages();

    if (isSignup && password !== confirmPassword) {
      setError("\uBE44\uBC00\uBC88\uD638 \uD655\uC778\uC774 \uC77C\uCE58\uD558\uC9C0 \uC54A\uC2B5\uB2C8\uB2E4.");
      return;
    }

    try {
      setSubmitting(true);

      const user = isSignup
        ? await signup({ username, nickname, password })
        : await login({ username, password });

      localStorage.setItem("chickenwikiUser", JSON.stringify(user));
      window.dispatchEvent(new Event("chickenwiki-auth-changed"));

      setSuccessMessage(
        isSignup
          ? "\uD68C\uC6D0\uAC00\uC785\uC774 \uC644\uB8CC\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uBC14\uB85C \uB85C\uADF8\uC778 \uC0C1\uD0DC\uB85C \uC774\uB3D9\uD569\uB2C8\uB2E4."
          : "\uB85C\uADF8\uC778\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uBA54\uC778 \uD398\uC774\uC9C0\uB85C \uC774\uB3D9\uD569\uB2C8\uB2E4."
      );

      window.setTimeout(() => {
        navigate("/");
      }, 700);
    } catch (err) {
      setError(err.message || "\uC694\uCCAD \uCC98\uB9AC \uC911 \uBB38\uC81C\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4.");
    } finally {
      setSubmitting(false);
    }
  };

  const inputStyle = {
    width: "100%",
    padding: 12,
    marginTop: 6,
    borderRadius: 12,
    border: "1px solid #2f3742",
    background: "#0f1217",
    color: "white",
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: 24,
      }}
    >
      <div
        style={{
          width: "100%",
          maxWidth: 460,
          padding: 28,
          borderRadius: 24,
          background:
            "radial-gradient(circle at top, rgba(246, 211, 101, 0.16), transparent 35%), linear-gradient(180deg, #1b1d22 0%, #111318 100%)",
          border: "1px solid #2b2f36",
          boxShadow: "0 24px 60px rgba(0, 0, 0, 0.3)",
        }}
      >
        <Link
          to="/"
          style={{
            display: "inline-flex",
            marginBottom: 20,
            color: "#f6d365",
            fontSize: 14,
          }}
        >
          {"\u2190 \uBA54\uC778\uC73C\uB85C \uB3CC\uC544\uAC00\uAE30"}
        </Link>

        <div style={{ display: "flex", gap: 10, marginBottom: 18 }}>
          {[
            { key: "login", label: "\uB85C\uADF8\uC778" },
            { key: "signup", label: "\uD68C\uC6D0\uAC00\uC785" },
          ].map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => {
                setMode(tab.key);
                resetMessages();
              }}
              style={{
                flex: 1,
                padding: "12px 14px",
                borderRadius: 14,
                border: tab.key === mode ? "1px solid #f6d365" : "1px solid #2f3742",
                background: tab.key === mode ? "#252019" : "#181b21",
                color: tab.key === mode ? "#fff2c4" : "#cdd6df",
                fontWeight: 700,
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <h2 style={{ marginTop: 0, marginBottom: 20, color: "#fff7df" }}>
          {isSignup ? "ChickenWiki \uD68C\uC6D0\uAC00\uC785" : "ChickenWiki \uB85C\uADF8\uC778"}
        </h2>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 12 }}>
            <label style={{ color: "#d8dee7", fontSize: 14, fontWeight: 600 }}>
              {"\uC544\uC774\uB514"}
              <input
                type="text"
                name="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={inputStyle}
              />
            </label>
          </div>

          {isSignup ? (
            <div style={{ marginBottom: 12 }}>
              <label style={{ color: "#d8dee7", fontSize: 14, fontWeight: 600 }}>
                {"\uB2C9\uB124\uC784"}
                <input
                  type="text"
                  name="nickname"
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  style={inputStyle}
                />
              </label>
            </div>
          ) : null}

          <div style={{ marginBottom: 12 }}>
            <label style={{ color: "#d8dee7", fontSize: 14, fontWeight: 600 }}>
              {"\uBE44\uBC00\uBC88\uD638"}
              <input
                type="password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={inputStyle}
              />
            </label>
          </div>

          {isSignup ? (
            <div style={{ marginBottom: 12 }}>
              <label style={{ color: "#d8dee7", fontSize: 14, fontWeight: 600 }}>
                {"\uBE44\uBC00\uBC88\uD638 \uD655\uC778"}
                <input
                  type="password"
                  name="confirmPassword"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  style={inputStyle}
                />
              </label>
            </div>
          ) : null}

          {error ? (
            <div
              style={{
                marginBottom: 12,
                padding: "12px 14px",
                borderRadius: 12,
                background: "#311d20",
                border: "1px solid #5a3038",
                color: "#ffccd3",
              }}
            >
              {error}
            </div>
          ) : null}

          {successMessage ? (
            <div
              style={{
                marginBottom: 12,
                padding: "12px 14px",
                borderRadius: 12,
                background: "#1f3124",
                border: "1px solid #385440",
                color: "#d6f5dd",
              }}
            >
              {successMessage}
            </div>
          ) : null}

          <button
            type="submit"
            disabled={submitting}
            style={{
              width: "100%",
              padding: "12px 16px",
              borderRadius: 14,
              border: "none",
              background: "#f6d365",
              color: "#15171b",
              fontWeight: 700,
              marginTop: 6,
              cursor: submitting ? "not-allowed" : "pointer",
              opacity: submitting ? 0.7 : 1,
            }}
          >
            {submitting
              ? isSignup
                ? "\uAC00\uC785 \uCC98\uB9AC \uC911..."
                : "\uB85C\uADF8\uC778 \uC911..."
              : isSignup
                ? "\uD68C\uC6D0\uAC00\uC785\uD558\uAE30"
                : "\uB85C\uADF8\uC778\uD558\uAE30"}
          </button>
        </form>
      </div>
    </div>
  );
}
