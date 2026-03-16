import React from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "./Header";

// 하드코딩된 브랜드별 메뉴 데이터
const brandMenus = {
  1: [ // 교촌치킨
    { id: 1, name: "허니콤보", price: "18000원", description: "달콤한 허니 소스와 치킨의 조화" },
    { id: 2, name: "레드콤보", price: "19000원", description: "매콤한 레드 소스와 치킨" },
    { id: 3, name: "오리지널콤보", price: "17000원", description: "클래식한 교촌치킨 맛" },
  ],
  2: [ // BBQ
    { id: 4, name: "황금올리브", price: "16000원", description: "바삭한 황금올리브 치킨" },
    { id: 5, name: "자메이카 통다리구이", price: "15000원", description: "통다리구이의 풍미" },
    { id: 6, name: "크런치 버거", price: "8000원", description: "치킨 패티 버거" },
  ],
  3: [ // 굽네치킨
    { id: 7, name: "고추바사삭", price: "17000원", description: "매콤한 고추와 바삭함" },
    { id: 8, name: "볼케이노", price: "18000원", description: "불맛 나는 치킨" },
    { id: 9, name: "갈릭바사삭", price: "17500원", description: "마늘 향이 가득한 치킨" },
  ],
};

const brandNames = {
  1: "교촌치킨",
  2: "BBQ",
  3: "굽네치킨",
};

export default function BrandDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const brandId = parseInt(id);
  const brandName = brandNames[brandId] || "알 수 없는 브랜드";
  const menus = brandMenus[brandId] || [];

  function MenuCard({ menu }) {
    return (
      <div style={{
        background: '#1a1a1a',
        padding: 16,
        borderRadius: 8,
        width: 280,
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        gap: 8,
        cursor: 'pointer',
        transition: 'transform 0.2s',
      }}
      onClick={() => navigate(`/menu/${menu.id}`)}
      onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
      onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
      >
        <div style={{ height: 150, background: '#0f0f0f', borderRadius: 6, marginBottom: 8 }} />
        <div style={{ fontWeight: 700, fontSize: 18 }}>{menu.name}</div>
        <div style={{ fontSize: 14, color: '#9aa6b2' }}>{menu.description}</div>
        <div style={{ fontSize: 16, fontWeight: 600, color: '#ffd700' }}>{menu.price}</div>
      </div>
    );
  }

  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: '0 auto' }}>
      <Header />
      <section style={{ marginTop: 24 }}>
        <h1 style={{ fontSize: '2em', marginBottom: 16 }}>{brandName} 메뉴</h1>
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', justifyContent: 'center' }}>
          {menus.length === 0 ? (
            <div style={{ color: '#9aa6b2' }}>메뉴가 없습니다.</div>
          ) : (
            menus.map((menu) => (
              <MenuCard key={menu.id} menu={menu} />
            ))
          )}
        </div>
      </section>
    </div>
  );
}