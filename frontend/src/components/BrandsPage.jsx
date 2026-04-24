import React from "react";
import Header from "./Header";

export default function BrandsPage() {
  return (
    <div style={{ minHeight: "100vh", background: "linear-gradient(180deg, #14171c 0%, #0d0f13 100%)" }}>
      <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto" }}>
        <Header />
        <section
          style={{
            marginTop: 24,
            padding: 28,
            borderRadius: 20,
            background: "linear-gradient(180deg, #1a1d23 0%, #13161b 100%)",
            border: "1px solid #2b3139",
          }}
        >
          <h2 style={{ marginTop: 0 }}>{"\uBE0C\uB79C\uB4DC \uBAA9\uB85D"}</h2>
          <p style={{ marginBottom: 0, color: "#9aa6b2" }}>
            {"\uC774 \uD398\uC774\uC9C0\uC5D0\uC11C\uB294 \uB4F1\uB85D\uB41C \uBE0C\uB79C\uB4DC\uB97C \uBAA8\uC544 \uBCFC \uC218 \uC788\uC2B5\uB2C8\uB2E4."}
          </p>
        </section>
      </div>
    </div>
  );
}
