import React, { useState } from "react";
import { useParams } from "react-router-dom";
import Header from "./Header";

// 하드코딩된 메뉴 데이터 (BrandDetailPage에서 복사)
const allMenus = [
  { id: 1, name: "허니콤보", price: "18000원", description: "달콤한 허니 소스와 치킨의 조화", brandId: 1 },
  { id: 2, name: "레드콤보", price: "19000원", description: "매콤한 레드 소스와 치킨", brandId: 1 },
  { id: 3, name: "오리지널콤보", price: "17000원", description: "클래식한 교촌치킨 맛", brandId: 1 },
  { id: 4, name: "황금올리브", price: "16000원", description: "바삭한 황금올리브 치킨", brandId: 2 },
  { id: 5, name: "자메이카 통다리구이", price: "15000원", description: "통다리구이의 풍미", brandId: 2 },
  { id: 6, name: "크런치 버거", price: "8000원", description: "치킨 패티 버거", brandId: 2 },
  { id: 7, name: "고추바사삭", price: "17000원", description: "매콤한 고추와 바삭함", brandId: 3 },
  { id: 8, name: "볼케이노", price: "18000원", description: "불맛 나는 치킨", brandId: 3 },
  { id: 9, name: "갈릭바사삭", price: "17500원", description: "마늘 향이 가득한 치킨", brandId: 3 },
];

// 하드코딩된 리뷰 데이터
const menuReviews = {
  1: [
    { id: 1, author: "치킨러버", content: "달콤하고 맛있어요! 추천합니다.", rating: 5, date: "2026-03-10", likes: 12 },
    { id: 2, author: "맵찌르", content: "허니가 너무 달아요. 다음엔 레드로 할게요.", rating: 3, date: "2026-03-08", likes: 3 },
  ],
  2: [
    { id: 3, author: "매콤러", content: "레드 소스가 정말 매콤해요. 좋아요!", rating: 4, date: "2026-03-09", likes: 8 },
  ],
  3: [
    { id: 4, author: "클래식", content: "항상 믿고 먹는 오리지널.", rating: 5, date: "2026-03-07", likes: 15 },
  ],
  4: [
    { id: 5, author: "바삭팬", content: "황금올리브는 바삭함이 최고!", rating: 5, date: "2026-03-06", likes: 7 },
  ],
  5: [
    { id: 6, author: "통다리", content: "통다리구이가 부드럽고 맛있네요.", rating: 4, date: "2026-03-05", likes: 5 },
  ],
  6: [
    { id: 7, author: "버거", content: "치킨 버거로 가볍게 먹기 좋음.", rating: 4, date: "2026-03-04", likes: 2 },
  ],
  7: [
    { id: 8, author: "고추", content: "고추바사삭은 매콤함이 적당해요.", rating: 4, date: "2026-03-03", likes: 9 },
  ],
  8: [
    { id: 9, author: "불맛", content: "볼케이노는 불맛이 강해서 좋아요.", rating: 5, date: "2026-03-02", likes: 11 },
  ],
  9: [
    { id: 10, author: "마늘", content: "갈릭 향이 풍부해서 맛있어요.", rating: 4, date: "2026-03-01", likes: 6 },
  ],
};

function ReviewItem({ review }) {
  const [likes, setLikes] = useState(review.likes || 0);
  const [liked, setLiked] = useState(false);

  const handleLike = () => {
    if (liked) {
      setLikes(likes - 1);
      setLiked(false);
    } else {
      setLikes(likes + 1);
      setLiked(true);
    }
  };

  return (
    <div style={{
      background: '#1a1a1a',
      padding: 16,
      borderRadius: 8,
      marginBottom: 12,
      color: 'white'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <div style={{ fontWeight: 600 }}>{review.author}</div>
        <div style={{ color: '#ffd700' }}>{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</div>
      </div>
      <div style={{ color: '#ccc', marginBottom: 12 }}>{review.content}</div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ fontSize: 12, color: '#777' }}>{review.date}</div>
        <button
          onClick={handleLike}
          style={{
            padding: '6px 12px',
            background: liked ? '#333' : '#ffd700',
            color: liked ? '#ffd700' : 'black',
            border: 'none',
            borderRadius: 4,
            cursor: 'pointer',
            fontSize: 12,
            display: 'flex',
            alignItems: 'center',
            gap: 4
          }}
        >
          👍 {likes}
        </button>
      </div>
    </div>
  );
}

function ReviewForm({ onSubmit }) {
  const [content, setContent] = useState('');
  const [rating, setRating] = useState(5);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!content.trim()) return;
    onSubmit({ content: content.trim(), rating });
    setContent('');
    setRating(5);
  };

  return (
    <form onSubmit={handleSubmit} style={{
      background: '#1a1a1a',
      padding: 20,
      borderRadius: 8,
      marginBottom: 24,
      display: 'flex',
      flexDirection: 'column',
      gap: 16
    }}>
      <h3 style={{ color: 'white', margin: 0 }}>리뷰 작성</h3>
      <div style={{ display: 'flex', gap: 16, alignItems: 'flex-start' }}>
        <textarea
          placeholder="리뷰 내용을 입력하세요"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
          style={{
            flex: 1,
            padding: '12px',
            borderRadius: 6,
            border: '1px solid #333',
            background: '#0f0f0f',
            color: 'white',
            resize: 'vertical',
            fontSize: 14
          }}
        />
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, minWidth: 120 }}>
          <label style={{ color: 'white', fontSize: 14 }}>평점</label>
          <select
            value={rating}
            onChange={(e) => setRating(parseInt(e.target.value))}
            style={{
              padding: '8px 12px',
              borderRadius: 6,
              border: '1px solid #333',
              background: '#0f0f0f',
              color: 'white',
              fontSize: 14
            }}
          >
            {[1,2,3,4,5].map(n => <option key={n} value={n}>{n}점</option>)}
          </select>
        </div>
      </div>
      <button type="submit" style={{
        alignSelf: 'flex-end',
        padding: '10px 20px',
        background: '#ffd700',
        color: 'black',
        border: 'none',
        borderRadius: 6,
        cursor: 'pointer',
        fontWeight: 600
      }}>
        리뷰 등록
      </button>
    </form>
  );
}

export default function MenuReviewPage() {
  const { id } = useParams();
  const menuId = parseInt(id);
  const menu = allMenus.find(m => m.id === menuId);
  const reviews = menuReviews[menuId] || [];

  const handleReviewSubmit = (newReview) => {
    // 실제로는 API로 전송, 여기서는 콘솔에 로그만
    console.log('새 리뷰:', newReview);
    alert('리뷰가 등록되었습니다! (임시 - 로그인 후 사용자 정보로 대체)');
  };

  if (!menu) {
    return (
      <div style={{ padding: 28, maxWidth: 1100, margin: '0 auto', color: 'white' }}>
        <Header />
        <p>메뉴를 찾을 수 없습니다.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: '0 auto' }}>
      <Header />

      <section style={{ marginTop: 24 }}>
        <div style={{
          background: '#1a1a1a',
          padding: 24,
          borderRadius: 8,
          display: 'flex',
          gap: 24,
          alignItems: 'center'
        }}>
          <div style={{ height: 150, width: 150, background: '#0f0f0f', borderRadius: 6 }} />
          <div style={{ color: 'white' }}>
            <h1 style={{ fontSize: '2em', marginBottom: 8 }}>{menu.name}</h1>
            <p style={{ color: '#9aa6b2', marginBottom: 8 }}>{menu.description}</p>
            <p style={{ fontSize: '1.2em', fontWeight: 600, color: '#ffd700' }}>{menu.price}</p>
          </div>
        </div>
      </section>

      <section style={{ marginTop: 36 }}>
        <h2 style={{ marginBottom: 16 }}>리뷰 ({reviews.length})</h2>
        <ReviewForm onSubmit={handleReviewSubmit} />
        <div>
          {reviews.length === 0 ? (
            <div style={{ color: '#9aa6b2', textAlign: 'center', padding: 24 }}>아직 리뷰가 없습니다.</div>
          ) : (
            reviews.map((review) => (
              <ReviewItem key={review.id} review={review} />
            ))
          )}
        </div>
      </section>
    </div>
  );
}