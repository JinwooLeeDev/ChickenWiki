import React from "react";

import { Link } from "react-router-dom";

export default function Header() {
  return (
    <header style={{
      display: "flex",
      alignItems: "center",
      gap: 24,
      padding: "12px 0"
    }}>
      <Link to="/" style={{ display: "flex", alignItems: "center", textDecoration: "none", color: "inherit" }}>
        <div style={{ width: 48, height: 48, borderRadius: 8, background: "#646cff" }} />
        <h1 style={{ margin: "0 0 0 8px", fontSize: 24 }}>ChickenWiki</h1>
      </Link>

      <nav style={{ marginLeft: "auto", display: "flex", gap: 16, fontSize: 15 }}>
        <Link to="/brands">브랜드</Link>
        <Link to="/login">로그인</Link>
      </nav>
    </header>
  );
}
