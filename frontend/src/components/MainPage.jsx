import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "./Header";
import { getBrandMenus, getBrands } from "../services/api";

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
};

export default function MainPage() {
  const navigate = useNavigate();
  const [menus, setMenus] = useState([]);
  const [filteredMenus, setFilteredMenus] = useState([]);
  const [q, setQ] = useState("");

  useEffect(() => {
    let mounted = true;

    async function loadMainPage() {
      const data = await getBrands();
      if (!mounted) return;
      if (!data || !Array.isArray(data)) return;

      const menuGroups = await Promise.all(
        data.map(async (brand) => {
          const items = await getBrandMenus(brand.id);
          return (items || []).map((menu) => ({
            ...menu,
            brandName: menu.brandName || brand.name,
          }));
        })
      );

      if (!mounted) return;
      setMenus(menuGroups.flat());
    }

    loadMainPage();

    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    const keyword = q.trim();
    if (!keyword || !menus.length) {
      setFilteredMenus([]);
      return;
    }

    setFilteredMenus(
      menus.filter((menu) => menu.menuName.toLowerCase().includes(keyword.toLowerCase()))
    );
  }, [menus, q]);

  return (
    <div style={pageStyles.pageBackdrop}>
      <div style={pageStyles.contentWrap}>
        <Header />

        <section
          style={{
            marginTop: 24,
            textAlign: "center",
            padding: "36px 28px",
            borderRadius: 18,
            background: "linear-gradient(180deg, #1a1d23 0%, #13161b 100%)",
            border: "1px solid #2b3139",
          }}
        >
        <h2 style={{ fontSize: "2.2em", marginBottom: 10, lineHeight: 1.2 }}>
          {"\uC804\uAD6D \uCE58\uD0A8 \uBE0C\uB79C\uB4DC \uC815\uBCF4, \uD55C\uB208\uC5D0"}
        </h2>
        <p style={{ color: "#9aa6b2", maxWidth: 720, margin: "0 auto", lineHeight: 1.6 }}>
          {
            "\uBA39\uACE0 \uC2F6\uC740 \uBA54\uB274 \uC774\uB984\uC744 \uAC80\uC0C9\uD558\uACE0, \uACB0\uACFC\uB97C \uB204\uB974\uBA74 \uBC14\uB85C \uD574\uB2F9 \uBA54\uB274 \uC0C1\uC138 \uD398\uC774\uC9C0\uB85C \uC774\uB3D9\uD574\uBCF4\uC138\uC694."
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
            background: "#171a20",
            border: "1px solid #2a2f38",
          }}
        >
        <input
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder={"\uBA54\uB274 \uC774\uB984\uC744 \uAC80\uC0C9\uD558\uC138\uC694"}
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
          onClick={() => setQ((value) => value.trim())}
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

        {q.trim() ? (
          <section
            style={{
              marginTop: 20,
              padding: 18,
              borderRadius: 16,
              background: "#151920",
              border: "1px solid #2a2f38",
            }}
          >
          <div style={{ color: "#9aa6b2", marginBottom: 8 }}>
            {`"${q}" \uAC80\uC0C9 \uACB0\uACFC`}
          </div>
          {filteredMenus.length > 0 ? (
            <div style={{ display: "grid", gap: 12 }}>
              {filteredMenus.map((menu) => (
                <button
                  key={menu.id}
                  type="button"
                  onClick={() => navigate(`/menu/${menu.id}`)}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    gap: 12,
                    width: "100%",
                    padding: "14px 16px",
                    borderRadius: 14,
                    border: "1px solid #2d3440",
                    background: "#181c22",
                    color: "white",
                    textAlign: "left",
                  }}
                >
                  <div>
                    <div style={{ fontSize: 15, fontWeight: 700, marginBottom: 4 }}>{menu.menuName}</div>
                    <div style={{ fontSize: 13, color: "#9aa6b2" }}>{menu.brandName}</div>
                  </div>
                  <div style={{ color: "#f6d365", fontSize: 13, fontWeight: 700 }}>
                    {"\uC0C1\uC138 \uBCF4\uAE30"}
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div style={{ color: "#9aa6b2" }}>
              {"\uC77C\uCE58\uD558\uB294 \uBA54\uB274\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4."}
            </div>
          )}
          </section>
        ) : null}

        <section
          style={{
            marginTop: 36,
            background: "#181c22",
            padding: 24,
            borderRadius: 16,
            border: "1px solid #2b3139",
          }}
        >
        <h3>{"\uC11C\uBE44\uC2A4 \uD2B9\uC9D5"}</h3>
        <ul style={{ color: "#ccc", lineHeight: 1.6 }}>
          <li>{"\uBA54\uB274 \uC774\uB984 \uAC80\uC0C9\uC73C\uB85C \uC6D0\uD558\uB294 \uBA54\uB274 \uBE60\uB974\uAC8C \uCC3E\uAE30"}</li>
          <li>{"\uBA54\uB274\uBCC4 \uC0C1\uC138 \uC815\uBCF4\uC640 \uAC00\uACA9 \uBE44\uAD50"}</li>
          <li>{"\uC0AC\uC6A9\uC790 \uB9AC\uBDF0 \uC791\uC131 \uBC0F \uD3C9\uC810 \uD655\uC778"}</li>
          <li>{"\uD5E4\uB354 \uBE0C\uB79C\uB4DC \uBAA9\uB85D\uC5D0\uC11C \uAD00\uC2EC \uBE0C\uB79C\uB4DC \uBC14\uB85C \uC774\uB3D9"}</li>
        </ul>
        </section>

        <footer style={{ marginTop: 48, textAlign: "center", color: "#777", fontSize: 13 }}>
          <div>{`Copyright ${new Date().getFullYear()} ChickenWiki. All rights reserved.`}</div>
        </footer>
      </div>
    </div>
  );
}
