import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "./Header";
import { getBrand, getBrandMenus } from "../services/api";

function formatPrice(value) {
  if (typeof value !== "number") {
    return value || "-";
  }

  return `${value.toLocaleString()}\uC6D0`;
}

export default function BrandDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const brandId = parseInt(id, 10);
  const [brand, setBrand] = useState(null);
  const [menus, setMenus] = useState([]);

  useEffect(() => {
    let mounted = true;

    Promise.all([getBrand(brandId), getBrandMenus(brandId)]).then(([brandData, menusData]) => {
      if (!mounted) return;
      setBrand(brandData);
      setMenus(menusData || []);
    });

    return () => {
      mounted = false;
    };
  }, [brandId]);

  function MenuCard({ menu }) {
    return (
      <div
        style={{
          background: "linear-gradient(180deg, #1f1f1f 0%, #171717 100%)",
          padding: 16,
          borderRadius: 14,
          width: 280,
          minHeight: 360,
          color: "white",
          display: "flex",
          flexDirection: "column",
          gap: 8,
          cursor: "pointer",
          transition: "transform 0.2s, box-shadow 0.2s",
          border: "1px solid #2a2a2a",
          boxShadow: "0 10px 24px rgba(0, 0, 0, 0.18)",
        }}
        onClick={() => navigate(`/menu/${menu.id}`)}
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
          src={menu.menuImageUrl}
          alt={`${menu.menuName} ${"\uC774\uBBF8\uC9C0"}`}
          style={{
            height: 150,
            width: "100%",
            objectFit: "cover",
            borderRadius: 10,
            marginBottom: 8,
            background: "#0f0f0f",
          }}
        />
        <div style={{ fontWeight: 700, fontSize: 18 }}>{menu.menuName}</div>
        <div
          style={{
            fontSize: 14,
            color: "#9aa6b2",
            lineHeight: 1.5,
            minHeight: 64,
            flexGrow: 1,
            overflow: "hidden",
          }}
        >
          {menu.description || "\uB4F1\uB85D\uB41C \uC124\uBA85\uC774 \uC5C6\uC2B5\uB2C8\uB2E4."}
        </div>
        <div
          style={{
            fontSize: 16,
            fontWeight: 600,
            color: "#ffd700",
            marginTop: "auto",
            paddingTop: 8,
          }}
        >
          {formatPrice(menu.menuPrice)}
        </div>
      </div>
    );
  }

  if (!brand) {
    return (
      <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto" }}>
        <Header />
        <div style={{ textAlign: "center", marginTop: 48, color: "#9aa6b2" }}>
          {"\uB85C\uB529 \uC911..."}
        </div>
      </div>
    );
  }

  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto" }}>
      <Header />
      <section style={{ marginTop: 24 }}>
        <h1 style={{ fontSize: "2em", marginBottom: 16 }}>{`${brand.name} \uBA54\uB274`}</h1>
        <div style={{ display: "flex", gap: 16, flexWrap: "wrap", justifyContent: "center", alignItems: "stretch" }}>
          {menus.length === 0 ? (
            <div style={{ color: "#9aa6b2" }}>{"\uBA54\uB274\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4."}</div>
          ) : (
            menus.map((menu) => <MenuCard key={menu.id} menu={menu} />)
          )}
        </div>
      </section>
    </div>
  );
}
