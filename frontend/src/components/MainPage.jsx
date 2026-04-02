import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "./Header";
import { getBrands, getBrandReviews } from "../services/api";

function BrandCard({ brand, reviews }) {
  const navigate = useNavigate();

  return (
    <div
      style={{
        background: "linear-gradient(180deg, #1f1f1f 0%, #171717 100%)",
        padding: 14,
        borderRadius: 14,
        width: 240,
        minHeight: 238,
        color: "white",
        display: "flex",
        flexDirection: "column",
        gap: 8,
        cursor: "pointer",
        transition: "transform 0.2s, box-shadow 0.2s",
        border: "1px solid #2a2a2a",
        boxShadow: "0 10px 24px rgba(0, 0, 0, 0.18)",
      }}
      onClick={() => navigate(`/brand/${brand.id}`)}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = "scale(1.05)";
        e.currentTarget.style.boxShadow = "0 18px 32px rgba(0, 0, 0, 0.28)";
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = "scale(1)";
        e.currentTarget.style.boxShadow = "0 10px 24px rgba(0, 0, 0, 0.18)";
      }}
    >
      <img
        src={brand.logoUrl}
        alt={`${brand.name} ${"\uB85C\uACE0"}`}
        style={{
          height: 100,
          width: "100%",
          objectFit: "cover",
          borderRadius: 10,
          marginBottom: 4,
          background: "#0f0f0f",
        }}
      />
      <div style={{ fontWeight: 700, fontSize: 17 }}>{brand.name}</div>
      {reviews && reviews.length > 0 ? (
        <div style={{ marginTop: 8 }}>
          <div style={{ fontSize: 13, color: "#9aa6b2", marginBottom: 4 }}>
            {"\uCD5C\uADFC \uB9AC\uBDF0"}
          </div>
          <ul
            style={{
              paddingLeft: 16,
              fontSize: 13,
              color: "#ccc",
              maxHeight: 60,
              overflow: "hidden",
              margin: 0,
            }}
          >
            {reviews.map((r) => (
              <li key={r.id} style={{ whiteSpace: "nowrap", textOverflow: "ellipsis", overflow: "hidden" }}>
                {r.author}: {r.content}
              </li>
            ))}
          </ul>
        </div>
      ) : (
        <div style={{ fontSize: 12, color: "#777", marginTop: 8 }}>
          {"\uB9AC\uBDF0 \uC5C6\uC74C"}
        </div>
      )}
    </div>
  );
}

export default function MainPage() {
  const [brands, setBrands] = useState(null);
  const [filtered, setFiltered] = useState(null);
  const [q, setQ] = useState("");
  const [reviewsMap, setReviewsMap] = useState({});

  useEffect(() => {
    let mounted = true;

    getBrands().then((data) => {
      if (!mounted) return;
      if (data && Array.isArray(data)) {
        setBrands(data);
        setFiltered(data);

        Promise.all(data.map((b) => getBrandReviews(b.id).then((rev) => [b.id, rev]))).then((pairs) => {
          if (!mounted) return;
          const map = {};
          pairs.forEach(([id, rev]) => {
            map[id] = rev;
          });
          setReviewsMap(map);
        });
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
            "\uB2E4\uC591\uD55C \uCE58\uD0A8 \uBE0C\uB79C\uB4DC\uC758 \uBA54\uB274\uC640 \uB9AC\uBDF0\uB97C \uBAA8\uC544 \uBE44\uAD50\uD558\uACE0, \uC5EC\uB7EC\uBD84\uC758 \uCDE8\uD5A5\uACFC \uACBD\uD5D8\uC744 \uACF5\uC720\uD574\uBCF4\uC138\uC694."
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
          placeholder={"\uBE0C\uB79C\uB4DC \uB610\uB294 \uBA54\uB274\uB97C \uAC80\uC0C9\uD558\uC138\uC694"}
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
          <li>{"\uBE0C\uB79C\uB4DC \uC990\uACA8\uCC3E\uAE30 \uBC0F \uAC1C\uC778\uD654 \uCD94\uCC9C(\uCD94\uD6C4)"}</li>
        </ul>
      </section>

      <section style={{ marginTop: 24 }}>
        <h2 style={{ marginBottom: 12 }}>{"\uBE0C\uB79C\uB4DC"}</h2>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(240px, 240px))",
            gap: 16,
            justifyContent: "center",
          }}
        >
          {!filtered || filtered.length === 0 ? (
            <div style={{ color: "#9aa6b2" }}>{"\uBE0C\uB79C\uB4DC\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4."}</div>
          ) : (
            filtered.map((b) => <BrandCard key={b.id} brand={b} reviews={reviewsMap[b.id] || []} />)
          )}
        </div>
      </section>

      <footer style={{ marginTop: 48, textAlign: "center", color: "#777", fontSize: 13 }}>
        <div>{`© ${new Date().getFullYear()} ChickenWiki. All rights reserved.`}</div>
      </footer>
    </div>
  );
}
