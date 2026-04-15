import React, { useEffect, useState } from "react";
import Header from "./Header";
import { getBrands } from "../services/api";

export default function MainPage() {
  const [brands, setBrands] = useState(null);
  const [filtered, setFiltered] = useState(null);
  const [q, setQ] = useState("");

  useEffect(() => {
    let mounted = true;

    getBrands().then((data) => {
      if (!mounted) return;
      if (data && Array.isArray(data)) {
        setBrands(data);
        setFiltered(data);
      }
    });

    return () => {
      mounted = false;
    };
  }, []);

  const handleSearch = () => {
    if (!brands) return;

    const keyword = q.trim();
    const hit =
      keyword.length === 0
        ? brands
        : brands.filter((b) => b.name.toLowerCase().includes(keyword.toLowerCase()));

    setFiltered(hit);
  };

  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto" }}>
      <Header />

      <section
        style={{
          marginTop: 24,
          textAlign: "center",
          padding: "36px 28px",
          borderRadius: 18,
          background: "radial-gradient(circle at top, rgba(255,215,0,0.12), rgba(255,215,0,0) 45%), #151515",
          border: "1px solid #272727",
        }}
      >
        <h2 style={{ fontSize: "2.2em", marginBottom: 10, lineHeight: 1.2 }}>
          {"\uC804\uAD6D \uCE58\uD0A8 \uBE0C\uB79C\uB4DC \uC815\uBCF4, \uD55C\uB208\uC5D0"}
        </h2>
        <p style={{ color: "#9aa6b2", maxWidth: 720, margin: "0 auto", lineHeight: 1.6 }}>
          {
            "\uD5E4\uB354\uC5D0\uC11C \uBE0C\uB79C\uB4DC \uBAA9\uB85D\uC744 \uBC14\uB85C \uD655\uC778\uD558\uACE0, \uAD81\uAE08\uD55C \uBE0C\uB79C\uB4DC \uC0C1\uC138 \uD398\uC774\uC9C0\uB85C \uBC14\uB85C \uC774\uB3D9\uD574\uBCF4\uC138\uC694."
          }
        </p>
      </section>

      <div
        style={{
          marginTop: 22,
          display: "flex",
          gap: 12,
          alignItems: "center",
          justifyContent: "center",
          flexWrap: "wrap",
          padding: 18,
          borderRadius: 16,
          background: "#161616",
          border: "1px solid #252525",
        }}
      >
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              handleSearch();
            }
          }}
          placeholder={"\uBE0C\uB79C\uB4DC\uB97C \uAC80\uC0C9\uD558\uC138\uC694"}
          style={{
            padding: "12px 14px",
            borderRadius: 12,
            border: "1px solid #303030",
            background: "#101010",
            color: "white",
            minWidth: 360,
            outline: "none",
          }}
        />
        <button
          onClick={handleSearch}
          style={{
            padding: "12px 18px",
            borderRadius: 12,
            border: "none",
            background: "#ffd700",
            color: "#161616",
            fontWeight: 700,
            cursor: "pointer",
          }}
        >
          {"\uAC80\uC0C9"}
        </button>
      </div>

      {q.trim() && filtered ? (
        <section
          style={{
            marginTop: 20,
            padding: 18,
            borderRadius: 16,
            background: "#141414",
            border: "1px solid #252525",
          }}
        >
          <div style={{ color: "#9aa6b2", marginBottom: 8 }}>
            {`"${q}" \uAC80\uC0C9 \uACB0\uACFC`}
          </div>
          <div style={{ color: "white" }}>
            {filtered.length > 0
              ? filtered.map((brand) => brand.name).join(", ")
              : "\uC77C\uCE58\uD558\uB294 \uBE0C\uB79C\uB4DC\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4."}
          </div>
        </section>
      ) : null}

      <section
        style={{
          marginTop: 36,
          background: "#1a1a1a",
          padding: 24,
          borderRadius: 16,
          border: "1px solid #292929",
        }}
      >
        <h3>{"\uC11C\uBE44\uC2A4 \uD2B9\uC9D5"}</h3>
        <ul style={{ color: "#ccc", lineHeight: 1.6 }}>
          <li>{"\uCE58\uD0A8 \uBE0C\uB79C\uB4DC\uBCC4 \uCD5C\uC2E0 \uBA54\uB274 \uC815\uBCF4\uB97C \uD55C\uB208\uC5D0 \uD655\uC778"}</li>
          <li>{"\uBA54\uB274\uBCC4 \uC0C1\uC138 \uC815\uBCF4\uC640 \uAC00\uACA9 \uBE44\uAD50"}</li>
          <li>{"\uC0AC\uC6A9\uC790 \uB9AC\uBDF0 \uC791\uC131 \uBC0F \uD3C9\uC810 \uD655\uC778"}</li>
          <li>{"\uD5E4\uB354 \uBE0C\uB79C\uB4DC \uBAA9\uB85D\uC5D0\uC11C \uAD00\uC2EC \uBE0C\uB79C\uB4DC \uBC14\uB85C \uC774\uB3D9"}</li>
        </ul>
      </section>

      <footer style={{ marginTop: 48, textAlign: "center", color: "#777", fontSize: 13 }}>
        <div>{`Copyright ${new Date().getFullYear()} ChickenWiki. All rights reserved.`}</div>
      </footer>
    </div>
  );
}
