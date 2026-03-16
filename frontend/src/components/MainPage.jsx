import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "./Header";
import { getBrands, getBrandReviews } from "../services/api";

function BrandCard({ brand, reviews }) {
  const navigate = useNavigate();

  return (
    <div
      style={{
        background: '#1a1a1a',
        padding: 12,
        borderRadius: 8,
        width: 240,
        color: 'white',
        display: 'flex',
        flexDirection: 'column',
        gap: 6,
        cursor: 'pointer',
        transition: 'transform 0.2s',
      }}
      onClick={() => navigate(`/brand/${brand.id}`)}
      onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
      onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
    >
      <div style={{ height: 100, background: '#0f0f0f', borderRadius: 6, marginBottom: 4 }} />
      <div style={{ fontWeight: 700, fontSize: 16 }}>{brand.name}</div>
      {reviews && reviews.length > 0 ? (
        <div style={{ marginTop: 8 }}>
          <div style={{ fontSize: 13, color: '#9aa6b2', marginBottom: 4 }}>최근 리뷰</div>
          <ul style={{ paddingLeft: 16, fontSize: 13, color: '#ccc', maxHeight: 60, overflow: 'hidden' }}>
            {reviews.map((r) => (
              <li key={r.id} style={{ whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden' }}>
                {r.author}: {r.content}
              </li>
            ))}
          </ul>
        </div>
      ) : (
        <div style={{ fontSize: 12, color: '#777', marginTop: 8 }}>리뷰 없음</div>
      )}
    </div>
  );
}

export default function MainPage() {
  const [brands, setBrands] = useState(null);
  const [filtered, setFiltered] = useState(null);
  const [q, setQ] = useState("");
  const [reviewsMap, setReviewsMap] = useState({});

  // load brands from backend and mirror to filtered
  useEffect(() => {
    let mounted = true;
    getBrands().then((data) => {
      if (!mounted) return;
      let list;
      if (data && Array.isArray(data)) {
        list = data;
      } else {
        list = [
          { id: 1, name: '교촌치킨' },
          { id: 2, name: 'BBQ' },
          { id: 3, name: '굽네치킨' },
        ];
      }
      setBrands(list);
      setFiltered(list);

      // fetch reviews for each brand
      Promise.all(
        list.map((b) =>
          getBrandReviews(b.id).then((rev) => [b.id, rev])
        )
      ).then((pairs) => {
        if (!mounted) return;
        const map = {};
        pairs.forEach(([id, rev]) => {
          map[id] = rev;
        });
        setReviewsMap(map);
      });
    });
    return () => (mounted = false);
  }, []);

  const handleSearch = () => {
    if (!brands) return;
    const hit = brands.filter((b) => b.name.includes(q));
    setFiltered(hit);
  };

  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: '0 auto' }}>
      <Header />

        {/* hero section */}
        <section style={{ marginTop: 24, textAlign: 'center' }}>
          <h2 style={{ fontSize: '2em', marginBottom: 8 }}>한국 치킨 브랜드 정보, 한눈에</h2>
          <p style={{ color: '#9aa6b2' }}>
            다양한 치킨 브랜드의 메뉴와 리뷰를 모아 비교하고, 여러분의 소중한 경험을 공유하세요.
          </p>
        </section>

        {/* search */}
        <div style={{ marginTop: 20, display: 'flex', gap: 12, alignItems: 'center', justifyContent: 'center' }}>
          <input
            value={q}
            onChange={(e) => setQ(e.target.value)}
            placeholder="브랜드 또는 메뉴 검색"
            style={{ padding: '10px 12px', borderRadius: 8, border: '1px solid #2a2a2a', background: 'transparent', color: 'white', minWidth: 360 }}
          />
          <button onClick={handleSearch} style={{ padding: '10px 16px' }}>검색</button>
        </div>

        {/* features / about */}
        <section style={{ marginTop: 36, background: '#1a1a1a', padding: 24, borderRadius: 8 }}>
          <h3>서비스 특징</h3>
          <ul style={{ color: '#ccc', lineHeight: 1.6 }}>
            <li>치킨 브랜드별 최신 메뉴 정보를 자동 크롤링</li>
            <li>메뉴별 상세 정보 및 가격 비교</li>
            <li>사용자 리뷰 작성 및 평점 확인</li>
            <li>브랜드 즐겨찾기 및 개인화 추천(추후)</li>
          </ul>
        </section>

        {/* list of brands */}
        <section style={{ marginTop: 24 }}>
          <h2 style={{ marginBottom: 12 }}>브랜드</h2>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', justifyContent: 'center' }}>
            {!filtered || filtered.length === 0 ? (
              <div style={{ color: '#9aa6b2' }}>브랜드가 없습니다.</div>
            ) : (
              filtered.map((b) => (
                <BrandCard key={b.id} brand={b} reviews={reviewsMap[b.id] || []} />
              ))
            )}
          </div>
        </section>

        {/* footer */}
        <footer style={{ marginTop: 48, textAlign: 'center', color: '#777', fontSize: 13 }}>
          <div>© {new Date().getFullYear()} ChickenWiki. All rights reserved.</div>
        </footer>
    </div>
  );
}
