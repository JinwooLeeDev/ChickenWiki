import React from "react";
import Header from "./Header";

export default function LoginPage() {
  return (
    <div style={{ padding: 28, maxWidth: 400, margin: "0 auto" }}>
      <Header />
      <h2>{"\uB85C\uADF8\uC778"}</h2>
      <form>
        <div style={{ marginBottom: 12 }}>
          <label>
            {"\uC544\uC774\uB514"}
            <input type="text" name="username" style={{ width: "100%", padding: 8, marginTop: 4 }} />
          </label>
        </div>
        <div style={{ marginBottom: 12 }}>
          <label>
            {"\uBE44\uBC00\uBC88\uD638"}
            <input type="password" name="password" style={{ width: "100%", padding: 8, marginTop: 4 }} />
          </label>
        </div>
        <button type="submit" style={{ padding: "10px 16px" }}>
          {"\uB85C\uADF8\uC778"}
        </button>
      </form>
    </div>
  );
}
