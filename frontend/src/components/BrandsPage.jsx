import React from "react";
import Header from "./Header";

export default function BrandsPage() {
  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto" }}>
      <Header />
      <h2>브랜드 목록</h2>
      <p>이 페이지에서는 등록된 브랜드를 볼 수 있습니다.</p>
      {/* 향후 API 데이터 렌더링 추가 */}
    </div>
  );
}
