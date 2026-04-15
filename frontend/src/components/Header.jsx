import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getBrands } from "../services/api";

export default function Header() {
  const [brands, setBrands] = useState([]);

  useEffect(() => {
    let mounted = true;

    getBrands().then((data) => {
      if (!mounted || !Array.isArray(data)) return;
      setBrands(data);
    });

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <header
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "stretch",
        gap: 18,
        padding: "12px 0",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 16,
          flexWrap: "wrap",
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

        <nav style={{ display: "flex", gap: 16, fontSize: 15, alignItems: "center" }}>
          <Link to="/login">{"\uB85C\uADF8\uC778"}</Link>
        </nav>
      </div>

      <section
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 10,
        }}
      >
        <div style={{ fontSize: 15, fontWeight: 700, color: "#f3f3f3" }}>
          {"\uBE0C\uB79C\uB4DC"}
        </div>

        <div
          style={{
            display: "flex",
            gap: 12,
            overflowX: "auto",
            paddingBottom: 8,
            scrollbarWidth: "thin",
          }}
        >
          {brands.length === 0 ? (
            <div
              style={{
                minWidth: 180,
                padding: "14px 16px",
                borderRadius: 14,
                background: "#181818",
                border: "1px solid #2b2b2b",
                color: "#9aa6b2",
              }}
            >
              {"\uBE0C\uB79C\uB4DC \uB85C\uB529 \uC911..."}
            </div>
          ) : (
            brands.map((brand) => (
              <Link
                key={brand.id}
                to={`/brand/${brand.id}`}
                style={{
                  flex: "0 0 auto",
                  minWidth: 140,
                  padding: "14px 18px",
                  borderRadius: 14,
                  background: "#1a1a1a",
                  border: "1px solid #303030",
                  color: "white",
                  textDecoration: "none",
                  textAlign: "center",
                  fontWeight: 600,
                  boxShadow: "0 8px 20px rgba(0, 0, 0, 0.18)",
                }}
              >
                {brand.name}
              </Link>
            ))
          )}
        </div>
      </section>
    </header>
  );
}
