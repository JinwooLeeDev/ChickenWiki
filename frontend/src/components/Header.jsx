import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getBrands } from "../services/api";

export default function Header() {
  const [brands, setBrands] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [brandsLoading, setBrandsLoading] = useState(true);
  const [brandsLoadFailed, setBrandsLoadFailed] = useState(false);

  useEffect(() => {
    let mounted = true;

    getBrands().then((data) => {
      if (!mounted) return;

      if (!Array.isArray(data)) {
        setBrands([]);
        setBrandsLoadFailed(true);
        setBrandsLoading(false);
        return;
      }

      setBrands(data);
      setBrandsLoadFailed(false);
      setBrandsLoading(false);
    });

    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    const syncUser = () => {
      try {
        const storedUser = localStorage.getItem("chickenwikiUser");
        setCurrentUser(storedUser ? JSON.parse(storedUser) : null);
      } catch (e) {
        console.error("Failed to read current user", e);
        setCurrentUser(null);
      }
    };

    syncUser();
    window.addEventListener("storage", syncUser);
    window.addEventListener("chickenwiki-auth-changed", syncUser);

    return () => {
      window.removeEventListener("storage", syncUser);
      window.removeEventListener("chickenwiki-auth-changed", syncUser);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("chickenwikiUser");
    setCurrentUser(null);
    window.dispatchEvent(new Event("chickenwiki-auth-changed"));
  };

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
          {currentUser ? (
            <>
              <div style={{ color: "#f6d365", fontWeight: 700 }}>
                {`${currentUser.nickname}\uB2D8`}
              </div>
              <Link
                to="/mypage"
                style={{
                  padding: "10px 14px",
                  borderRadius: 10,
                  border: "1px solid #3a404a",
                  background: "#181b21",
                  color: "#f5f7fa",
                  fontWeight: 600,
                }}
              >
                {"\uB9C8\uC774\uD398\uC774\uC9C0"}
              </Link>
              <button
                type="button"
                onClick={handleLogout}
                style={{
                  padding: "10px 14px",
                  borderRadius: 10,
                  border: "1px solid #3a404a",
                  background: "#181b21",
                  color: "#f5f7fa",
                  fontWeight: 600,
                }}
              >
                {"\uB85C\uADF8\uC544\uC6C3"}
              </button>
            </>
          ) : (
            <Link to="/login">{"\uB85C\uADF8\uC778"}</Link>
          )}
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
          {brandsLoading ? (
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
          ) : brandsLoadFailed ? (
            <div
              style={{
                minWidth: 260,
                padding: "14px 16px",
                borderRadius: 14,
                background: "#23181a",
                border: "1px solid #503239",
                color: "#ffccd3",
              }}
            >
              {"\uBE0C\uB79C\uB4DC \uBAA9\uB85D\uC744 \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uBC31\uC5D4\uB4DC \uC11C\uBC84 \uC0C1\uD0DC\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694."}
            </div>
          ) : brands.length === 0 ? (
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
              {"\uB4F1\uB85D\uB41C \uBE0C\uB79C\uB4DC\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4."}
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
