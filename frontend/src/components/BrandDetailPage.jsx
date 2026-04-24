import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "./Header";
import { getBrand, getBrandMenus } from "../services/api";

const pageStyles = {
  pageBackdrop: {
    minHeight: "100vh",
    background: "linear-gradient(180deg, #14171c 0%, #0d0f13 100%)",
  },
  contentWrap: {
    padding: 28,
    maxWidth: 1100,
    margin: "0 auto",
  },
  pageShell: {
    marginTop: 24,
    padding: "28px 24px 32px",
    borderRadius: 24,
    background: "linear-gradient(180deg, #181b21 0%, #111318 100%)",
    border: "1px solid #252b33",
    boxShadow: "0 24px 48px rgba(0, 0, 0, 0.2)",
  },
  hero: {
    padding: "32px 28px",
    borderRadius: 20,
    background: "linear-gradient(180deg, #20242b 0%, #171a20 100%)",
    border: "1px solid #303744",
    boxShadow: "0 18px 36px rgba(0, 0, 0, 0.16)",
  },
  menuGrid: {
    display: "flex",
    gap: 16,
    flexWrap: "wrap",
    justifyContent: "center",
    alignItems: "stretch",
  },
  menuCard: {
    background: "linear-gradient(180deg, #1a1d23 0%, #13161b 100%)",
    padding: 16,
    borderRadius: 16,
    width: 280,
    minHeight: 360,
    color: "white",
    display: "flex",
    flexDirection: "column",
    gap: 8,
    cursor: "pointer",
    transition: "transform 0.2s, box-shadow 0.2s, border-color 0.2s",
    border: "1px solid #2d333c",
    boxShadow: "0 12px 28px rgba(0, 0, 0, 0.18)",
  },
};

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
        style={pageStyles.menuCard}
        onClick={() => navigate(`/menu/${menu.id}`)}
        onMouseEnter={(e) => {
          e.currentTarget.style.transform = "scale(1.05)";
          e.currentTarget.style.boxShadow = "0 18px 32px rgba(0, 0, 0, 0.28)";
          e.currentTarget.style.borderColor = "#495363";
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.transform = "scale(1)";
          e.currentTarget.style.boxShadow = pageStyles.menuCard.boxShadow;
          e.currentTarget.style.borderColor = "#2d333c";
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
      <div style={pageStyles.pageBackdrop}>
        <div style={pageStyles.contentWrap}>
          <Header />
          <div style={{ textAlign: "center", marginTop: 48, color: "#9aa6b2" }}>
            {"\uB85C\uB529 \uC911..."}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={pageStyles.pageBackdrop}>
      <div style={pageStyles.contentWrap}>
        <Header />
        <section style={pageStyles.pageShell}>
          <div style={pageStyles.hero}>
            <div style={{ color: "#c8d0da", fontSize: 13, fontWeight: 700, marginBottom: 10 }}>
              {"\uBE0C\uB79C\uB4DC \uC0C1\uC138"}
            </div>
            <h1 style={{ fontSize: "2em", marginTop: 0, marginBottom: 10 }}>{`${brand.name} \uBA54\uB274`}</h1>
            <p style={{ marginTop: 0, marginBottom: 24, color: "#9aa6b2", lineHeight: 1.6 }}>
              {"\uBE0C\uB79C\uB4DC\uB97C \uBC14\uAFD4\uB3C4 \uD398\uC774\uC9C0 \uBC30\uACBD \uD1A4\uC740 \uAC19\uAC8C \uC720\uC9C0\uB418\uB3C4\uB85D \uC815\uB9AC\uD55C \uBA54\uB274 \uBAA9\uB85D\uC785\uB2C8\uB2E4."}
            </p>
          </div>
          <div style={{ ...pageStyles.menuGrid, marginTop: 24 }}>
            {menus.length === 0 ? (
              <div style={{ color: "#9aa6b2" }}>{"\uBA54\uB274\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4."}</div>
            ) : (
              menus.map((menu) => <MenuCard key={menu.id} menu={menu} />)
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
