import React from "react";
import Header from "./Header";

export default function LoginPage() {
  return (
    <div style={{ padding: 28, maxWidth: 400, margin: "0 auto" }}>
      <Header />
      <h2>로그인</h2>
      <form>
        <div style={{ marginBottom: 12 }}>
          <label>
            아이디
            <input type="text" name="username" style={{ width: "100%", padding: 8, marginTop: 4 }} />
          </label>
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>
            비밀번호
            <input type="password" name="password" style={{ width: "100%", padding: 8, marginTop: 4 }} />
          </label>
        </div>
        <button type="submit" style={{ padding: "10px 16px" }}>로그인</button>
      </form>
    </div>
  );
}
