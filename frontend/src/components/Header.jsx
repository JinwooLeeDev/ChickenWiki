import React from "react";
import { Link } from "react-router-dom";

export default function Header() {
  return (
    <header
      style={{
        display: "flex",
        alignItems: "center",
        gap: 24,
        padding: "12px 0",
      }}
    >
      <Link
        to="/"
        style={{ display: "flex", alignItems: "center", textDecoration: "none", color: "inherit" }}
      >
        <img
          src="/ChickenWikiLogo.png"
          alt="ChickenWiki logo"
          style={{
            width: 150,
            height: 150,
            objectFit: "contain",
            display: "block",
          }}
        />
        <h1 style={{ margin: "0 0 0 8px", fontSize: 24 }}>ChickenWiki</h1>
      </Link>

      <nav style={{ marginLeft: "auto", display: "flex", gap: 16, fontSize: 15 }}>
        <Link to="/brands">{"\uBE0C\uB79C\uB4DC"}</Link>
        <Link to="/login">{"\uB85C\uADF8\uC778"}</Link>
      </nav>
    </header>
  );
}
