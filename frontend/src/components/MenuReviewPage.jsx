import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Header from "./Header";
import {
  createMenuReview,
  deleteMenuReview,
  getMenu,
  getMenuReviews,
  recommendMenuReview,
  updateMenuReview,
} from "../services/api";

const REVIEWS_PER_PAGE = 15;

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

function formatDate(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
}

function formatPrice(value) {
  if (typeof value !== "number") {
    return value || "-";
  }

  return `${value.toLocaleString()}원`;
}

function toTimestamp(value) {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? 0 : date.getTime();
}

function sortReviews(reviews, sortOption) {
  const sorted = [...reviews];

  sorted.sort((a, b) => {
    if (sortOption === "rating") {
      if (b.rating !== a.rating) {
        return b.rating - a.rating;
      }
      return toTimestamp(b.createdAt) - toTimestamp(a.createdAt);
    }

    if (sortOption === "likes") {
      const likeGap = (b.likeCount || 0) - (a.likeCount || 0);
      if (likeGap !== 0) {
        return likeGap;
      }
      if (b.rating !== a.rating) {
        return b.rating - a.rating;
      }
      return toTimestamp(b.createdAt) - toTimestamp(a.createdAt);
    }

    const latestGap = toTimestamp(b.createdAt) - toTimestamp(a.createdAt);
    if (latestGap !== 0) {
      return latestGap;
    }
    return b.rating - a.rating;
  });

  return sorted;
}

function readStoredUser() {
  try {
    const storedUser = localStorage.getItem("chickenwikiUser");
    return storedUser ? JSON.parse(storedUser) : null;
  } catch (e) {
    console.error("Failed to read current user", e);
    return null;
  }
}

function isAuthErrorMessage(message) {
  if (!message) return false;

  const normalized = message.toLowerCase();
  return (
    normalized.includes("authorization") ||
    normalized.includes("unauthorized") ||
    normalized.includes("forbidden") ||
    normalized.includes("token")
  );
}

function ReviewItem({
  review,
  currentUser,
  editingReviewId,
  actionLoading,
  recommendLoading,
  onStartEdit,
  onCancelEdit,
  onSaveEdit,
  onDelete,
  onRecommend,
  onOpenAuthorProfile,
}) {
  const isOwner = currentUser?.nickname === review.author;
  const isAdmin = currentUser?.role === "ADMIN";
  const canDelete = isOwner || isAdmin;
  const isEditing = editingReviewId === review.id;
  const [content, setContent] = useState(review.content);
  const [rating, setRating] = useState(review.rating);

  useEffect(() => {
    setContent(review.content);
    setRating(review.rating);
  }, [review]);

  const handleSave = async () => {
    if (!content.trim()) {
      alert("리뷰 내용을 입력해 주세요.");
      return;
    }

    await onSaveEdit(review.id, {
      content: content.trim(),
      rating,
    });
  };

  const recommendButtonDisabled = recommendLoading;

  return (
    <div
      style={{
        background: "#1a1a1a",
        padding: "18px 18px 16px",
        borderRadius: 10,
        marginBottom: 12,
        color: "white",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
          gap: 12,
        }}
      >
        <div style={{ flex: 1, minWidth: 0 }}>
          <div
            style={{
              display: "flex",
              alignItems: "center",
              gap: 10,
              flexWrap: "wrap",
              marginBottom: 6,
            }}
          >
            {isAdmin ? (
              <button
                type="button"
                onClick={() => onOpenAuthorProfile(review.author)}
                style={{
                  padding: 0,
                  border: "none",
                  background: "transparent",
                  color: "white",
                  fontWeight: 600,
                  cursor: "pointer",
                  textDecoration: "underline",
                  textUnderlineOffset: 3,
                }}
              >
                {review.author}
              </button>
            ) : (
              <div style={{ fontWeight: 600 }}>{review.author}</div>
            )}
            <div style={{ fontSize: 12, color: "#777" }}>{formatDate(review.createdAt)}</div>
          </div>
          <div style={{ color: "#ffd700", marginBottom: 12 }}>
            {"★".repeat(isEditing ? rating : review.rating)}
            {"☆".repeat(5 - (isEditing ? rating : review.rating))}
          </div>
          {isEditing ? (
            <div style={{ display: "grid", gap: 12 }}>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={4}
                style={{
                  width: "100%",
                  padding: "12px 14px",
                  borderRadius: 12,
                  border: "1px solid #2c3138",
                  background: "#101318",
                  color: "white",
                  resize: "vertical",
                }}
              />
              <select
                value={rating}
                onChange={(e) => setRating(parseInt(e.target.value, 10))}
                style={{
                  width: 120,
                  padding: "10px 12px",
                  borderRadius: 10,
                  border: "1px solid #333942",
                  background: "#171a20",
                  color: "white",
                }}
              >
                {[1, 2, 3, 4, 5].map((n) => (
                  <option key={n} value={n}>
                    {`${n}점`}
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <div style={{ color: "#ccc", lineHeight: 1.7 }}>{review.content}</div>
          )}
        </div>
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "flex-end",
            gap: 8,
            minWidth: 148,
            flexShrink: 0,
          }}
        >
          <button
            type="button"
            onClick={() => onRecommend(review)}
            disabled={recommendButtonDisabled}
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: 6,
              padding: "7px 12px",
              borderRadius: 999,
              border: review.likedByCurrentUser ? "1px solid #6e5a1b" : "1px solid #39414d",
              background: review.likedByCurrentUser ? "#2d2614" : "#181b21",
              color: review.likedByCurrentUser ? "#ffe29a" : "#d7dee7",
              fontSize: 13,
              fontWeight: 700,
              cursor: recommendButtonDisabled ? "not-allowed" : "pointer",
              opacity: recommendLoading ? 0.7 : 1,
            }}
          >
            <span>👍</span>
            <span>{review.likedByCurrentUser ? "추천 완료" : "추천"}</span>
            <span>{review.likeCount || 0}</span>
          </button>
          {canDelete ? (
            isEditing ? (
              <div style={{ display: "flex", gap: 6, marginTop: 4 }}>
                <button
                  type="button"
                  onClick={handleSave}
                  disabled={actionLoading}
                  style={{
                    padding: "7px 11px",
                    borderRadius: 10,
                    border: "none",
                    background: "#f6d365",
                    color: "#17191d",
                    fontWeight: 700,
                    fontSize: 13,
                    opacity: actionLoading ? 0.7 : 1,
                  }}
                >
                  {actionLoading ? "저장 중" : "저장"}
                </button>
                <button
                  type="button"
                  onClick={onCancelEdit}
                  disabled={actionLoading}
                  style={{
                    padding: "7px 11px",
                    borderRadius: 10,
                    border: "1px solid #39414d",
                    background: "#181b21",
                    color: "white",
                    fontSize: 13,
                  }}
                >
                  취소
                </button>
              </div>
            ) : (
              <div style={{ display: "flex", gap: 6, marginTop: 4 }}>
                {isOwner ? (
                  <button
                    type="button"
                    onClick={() => onStartEdit(review.id)}
                    disabled={actionLoading}
                    style={{
                      padding: "7px 11px",
                      borderRadius: 10,
                      border: "1px solid #39414d",
                      background: "#181b21",
                      color: "white",
                      fontSize: 13,
                    }}
                  >
                    수정
                  </button>
                ) : null}
                <button
                  type="button"
                  onClick={() => onDelete(review.id)}
                  disabled={actionLoading}
                  style={{
                    padding: "7px 11px",
                    borderRadius: 10,
                    border: "1px solid #5a3038",
                    background: "#2b171b",
                    color: "#ffccd3",
                    fontSize: 13,
                  }}
                >
                  {isOwner ? "삭제" : "관리자 삭제"}
                </button>
              </div>
            )
          ) : null}
        </div>
      </div>
    </div>
  );
}

function ReviewForm({ currentUser, onSubmit, submitting }) {
  const [content, setContent] = useState("");
  const [rating, setRating] = useState(5);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!currentUser) return;
    if (!content.trim()) return;

    const success = await onSubmit({
      content: content.trim(),
      rating,
    });

    if (success) {
      setContent("");
      setRating(5);
    }
  };

  if (!currentUser) {
    return (
      <div
        style={{
          marginBottom: 24,
          padding: 24,
          borderRadius: 18,
          background: "linear-gradient(180deg, #1a1c21 0%, #14161b 100%)",
          border: "1px solid #2d3138",
          color: "white",
        }}
      >
        <h3 style={{ marginTop: 0, marginBottom: 8 }}>리뷰 작성</h3>
        <div style={{ color: "#9aa6b2", lineHeight: 1.6, marginBottom: 16 }}>
          리뷰를 남기려면 먼저 로그인해 주세요.
        </div>
        <Link
          to="/login"
          style={{
            display: "inline-flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "10px 16px",
            borderRadius: 12,
            background: "#f6d365",
            color: "#17191d",
            textDecoration: "none",
            fontWeight: 700,
          }}
        >
          로그인하러 가기
        </Link>
      </div>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        background: "linear-gradient(180deg, #1a1c21 0%, #14161b 100%)",
        padding: 24,
        borderRadius: 18,
        marginBottom: 24,
        display: "flex",
        flexDirection: "column",
        gap: 16,
        border: "1px solid #2d3138",
        boxShadow: "0 18px 36px rgba(0, 0, 0, 0.18)",
      }}
    >
      <h3 style={{ color: "white", margin: 0 }}>리뷰 작성</h3>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 12,
          padding: "14px 16px",
          borderRadius: 14,
          background: "#101318",
          border: "1px solid #2c3138",
          color: "#dbe3ec",
        }}
      >
        <div>
          <div style={{ fontSize: 12, color: "#8f9aa7", marginBottom: 4 }}>작성자</div>
          <div style={{ fontWeight: 700 }}>{currentUser.nickname}</div>
        </div>
        <div style={{ fontSize: 12, color: "#8f9aa7" }}>
          닉네임은 계정 정보에서 자동 적용됩니다.
        </div>
      </div>
      <div style={{ display: "flex", gap: 16, alignItems: "flex-start", flexWrap: "wrap" }}>
        <textarea
          placeholder="메뉴에 대한 솔직한 후기를 남겨주세요"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
          style={{
            flex: 1,
            minWidth: 260,
            minHeight: 136,
            padding: "14px 16px",
            borderRadius: 14,
            border: "1px solid #2c3138",
            background: "#101318",
            color: "white",
            resize: "vertical",
            fontSize: 14,
            lineHeight: 1.6,
          }}
        />
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 10,
            minWidth: 160,
            padding: "14px 16px",
            borderRadius: 14,
            background: "#101318",
            border: "1px solid #2c3138",
          }}
        >
          <label style={{ color: "white", fontSize: 14, fontWeight: 700 }}>평점</label>
          <select
            value={rating}
            onChange={(e) => setRating(parseInt(e.target.value, 10))}
            style={{
              padding: "10px 12px",
              borderRadius: 10,
              border: "1px solid #333942",
              background: "#171a20",
              color: "white",
              fontSize: 14,
            }}
          >
            {[1, 2, 3, 4, 5].map((n) => (
              <option key={n} value={n}>
                {`${n}점`}
              </option>
            ))}
          </select>
        </div>
      </div>
      <button
        type="submit"
        disabled={submitting}
        style={{
          alignSelf: "flex-end",
          padding: "12px 22px",
          background: "#f6d365",
          color: "#17191d",
          border: "none",
          borderRadius: 12,
          cursor: submitting ? "not-allowed" : "pointer",
          fontWeight: 700,
          opacity: submitting ? 0.7 : 1,
        }}
      >
        {submitting ? "등록 중..." : "리뷰 등록"}
      </button>
    </form>
  );
}

export default function MenuReviewPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const menuId = parseInt(id, 10);
  const [menu, setMenu] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [sortOption, setSortOption] = useState("likes");
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [reviewActionLoading, setReviewActionLoading] = useState(false);
  const [recommendLoadingId, setRecommendLoadingId] = useState(null);
  const [editingReviewId, setEditingReviewId] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [error, setError] = useState("");
  const [toast, setToast] = useState({ id: 0, message: "", hiding: false });
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    const syncUser = () => {
      const nextUser = readStoredUser();
      setCurrentUser(nextUser);

      if (!nextUser) {
        setEditingReviewId(null);
      }
    };

    syncUser();
    window.addEventListener("storage", syncUser);
    window.addEventListener("chickenwiki-auth-changed", syncUser);

    return () => {
      window.removeEventListener("storage", syncUser);
      window.removeEventListener("chickenwiki-auth-changed", syncUser);
    };
  }, []);

  useEffect(() => {
    let mounted = true;

    async function loadMenuPage() {
      setLoading(true);
      setError("");

      try {
        const [menuData, reviewData] = await Promise.all([getMenu(menuId), getMenuReviews(menuId)]);

        if (!mounted) return;

        if (!menuData) {
          setMenu(null);
          setReviews([]);
          setError("menu-not-found");
          return;
        }

        setMenu(menuData);
        setReviews(reviewData || []);
      } catch (e) {
        if (!mounted) return;
        setMenu(null);
        setReviews([]);
        setError("menu-not-found");
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    if (Number.isNaN(menuId)) {
      setMenu(null);
      setReviews([]);
      setError("menu-not-found");
      setLoading(false);
      return undefined;
    }

    loadMenuPage();
    return () => {
      mounted = false;
    };
  }, [menuId, currentUser?.token]);

  useEffect(() => {
    setCurrentPage(1);
  }, [sortOption, reviews.length]);

  useEffect(() => {
    if (!toast.message) {
      return undefined;
    }

    const hideTimeoutId = window.setTimeout(() => {
      setToast((prev) => (prev.id === toast.id ? { ...prev, hiding: true } : prev));
    }, 1500);

    const clearTimeoutId = window.setTimeout(() => {
      setToast((prev) => (prev.id === toast.id ? { id: 0, message: "", hiding: false } : prev));
    }, 1900);

    return () => {
      window.clearTimeout(hideTimeoutId);
      window.clearTimeout(clearTimeoutId);
    };
  }, [toast]);

  const showToast = (message) => {
    setToast({
      id: Date.now(),
      message,
      hiding: false,
    });
  };

  const handleReviewSubmit = async (newReview) => {
    if (!currentUser?.token) {
      alert("리뷰 작성은 로그인 후 이용할 수 있어요.");
      return false;
    }

    try {
      setSubmitting(true);
      const createdReview = await createMenuReview(menuId, newReview);
      setReviews((prev) => [createdReview, ...prev]);
      setCurrentPage(1);
      showToast("리뷰를 작성했습니다.");
      return true;
    } catch (e) {
      alert(
        isAuthErrorMessage(e.message)
          ? "로그인한 사용자만 리뷰를 작성할 수 있어요."
          : e.message || "리뷰 등록에 실패했습니다."
      );
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  const handleSaveEdit = async (reviewId, payload) => {
    if (!currentUser?.token) {
      setEditingReviewId(null);
      alert("리뷰 수정은 로그인 상태에서만 가능해요.");
      return;
    }

    try {
      setReviewActionLoading(true);
      const updatedReview = await updateMenuReview(menuId, reviewId, payload);
      setReviews((prev) => prev.map((review) => (review.id === reviewId ? updatedReview : review)));
      setEditingReviewId(null);
      showToast("리뷰를 수정했습니다.");
    } catch (e) {
      if (isAuthErrorMessage(e.message)) {
        setEditingReviewId(null);
        alert("리뷰 수정은 로그인 상태에서만 가능해요.");
        return;
      }

      alert(e.message || "리뷰 수정에 실패했습니다.");
    } finally {
      setReviewActionLoading(false);
    }
  };

  const handleDelete = async (reviewId) => {
    if (!currentUser?.token) {
      alert("리뷰 삭제는 로그인 상태에서만 가능해요.");
      return;
    }

    const targetReview = reviews.find((review) => review.id === reviewId);
    const isOwner = targetReview?.author === currentUser?.nickname;
    const confirmMessage = isOwner
      ? "리뷰를 삭제하시겠습니까?"
      : "관리자 권한으로 이 리뷰를 삭제하시겠습니까?";

    if (!window.confirm(confirmMessage)) {
      return;
    }

    try {
      setReviewActionLoading(true);
      await deleteMenuReview(menuId, reviewId);
      setReviews((prev) => prev.filter((review) => review.id !== reviewId));
      if (editingReviewId === reviewId) {
        setEditingReviewId(null);
      }
      showToast("리뷰를 삭제했습니다.");
    } catch (e) {
      if (isAuthErrorMessage(e.message)) {
        setEditingReviewId(null);
        alert("리뷰 삭제는 로그인 상태에서만 가능해요.");
        return;
      }

      alert(e.message || "리뷰 삭제에 실패했습니다.");
    } finally {
      setReviewActionLoading(false);
    }
  };

  const handleOpenAuthorProfile = (nickname) => {
    if (currentUser?.role !== "ADMIN") {
      return;
    }

    navigate(`/admin/users/${encodeURIComponent(nickname)}`);
  };

  const handleRecommend = async (review) => {
    if (!currentUser?.token) {
      alert("리뷰 추천은 로그인 후 이용할 수 있어요.");
      return;
    }

    try {
      setRecommendLoadingId(review.id);
      const updatedReview = await recommendMenuReview(menuId, review.id);
      setReviews((prev) => prev.map((item) => (item.id === review.id ? updatedReview : item)));
      showToast(updatedReview.likedByCurrentUser ? "추천했습니다." : "추천을 취소했습니다.");
    } catch (e) {
      alert(
        isAuthErrorMessage(e.message)
          ? "리뷰 추천은 로그인 후 이용할 수 있어요."
          : e.message || "리뷰 추천에 실패했습니다."
      );
    } finally {
      setRecommendLoadingId(null);
    }
  };

  const sortedReviews = sortReviews(reviews, sortOption);
  const totalPages = Math.ceil(sortedReviews.length / REVIEWS_PER_PAGE);
  const safeCurrentPage = totalPages === 0 ? 1 : Math.min(currentPage, totalPages);
  const pagedReviews =
    totalPages <= 1
      ? sortedReviews
      : sortedReviews.slice(
          (safeCurrentPage - 1) * REVIEWS_PER_PAGE,
          safeCurrentPage * REVIEWS_PER_PAGE
        );

  if (loading) {
    return (
      <div style={pageStyles.pageBackdrop}>
        <div style={{ ...pageStyles.contentWrap, color: "white" }}>
          <Header />
          <p>메뉴 정보를 불러오는 중입니다...</p>
        </div>
      </div>
    );
  }

  if (!menu || error === "menu-not-found") {
    return (
      <div style={pageStyles.pageBackdrop}>
        <div style={{ ...pageStyles.contentWrap, color: "white" }}>
          <Header />
          <p>메뉴를 찾을 수 없습니다.</p>
        </div>
      </div>
    );
  }

  return (
    <div style={pageStyles.pageBackdrop}>
      <div style={pageStyles.contentWrap}>
        <Header />

        <section style={{ marginTop: 24 }}>
          <div
            style={{
              background: "#1a1a1a",
              padding: 24,
              borderRadius: 8,
              display: "flex",
              gap: 24,
              alignItems: "center",
              flexWrap: "wrap",
            }}
          >
            <img
              src={menu.menuImageUrl}
              alt={menu.menuName}
              style={{
                height: 180,
                width: 180,
                objectFit: "cover",
                borderRadius: 8,
                background: "#0f0f0f",
              }}
            />
            <div style={{ color: "white", flex: 1, minWidth: 240 }}>
              <div style={{ color: "#9aa6b2", marginBottom: 8 }}>{menu.brandName}</div>
              <h1 style={{ fontSize: "2em", marginBottom: 8 }}>{menu.menuName}</h1>
              <p style={{ color: "#9aa6b2", marginBottom: 8 }}>
                {menu.description || "등록된 메뉴 설명이 없습니다."}
              </p>
              <p style={{ fontSize: "1.2em", fontWeight: 600, color: "#ffd700" }}>
                {formatPrice(menu.menuPrice)}
              </p>
            </div>
          </div>
        </section>

        <section style={{ marginTop: 36 }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              gap: 16,
              marginBottom: 16,
              flexWrap: "wrap",
            }}
          >
            <h2 style={{ margin: 0 }}>{`리뷰 ${reviews.length}개`}</h2>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: 10,
                padding: "8px 12px",
                borderRadius: 12,
                background: "#171a20",
                border: "1px solid #2c3138",
              }}
            >
              <span style={{ color: "#9aa6b2", fontSize: 13, fontWeight: 700 }}>정렬</span>
              <select
                value={sortOption}
                onChange={(e) => setSortOption(e.target.value)}
                style={{
                  padding: "8px 28px 8px 10px",
                  borderRadius: 10,
                  border: "1px solid #333942",
                  background: "#101318",
                  color: "white",
                  fontSize: 13,
                  fontWeight: 700,
                  cursor: "pointer",
                }}
              >
                <option value="latest">최신순</option>
                <option value="rating">평점순</option>
                <option value="likes">추천순</option>
              </select>
            </div>
          </div>
          <div>
            {reviews.length === 0 ? (
              <div style={{ color: "#9aa6b2", textAlign: "center", padding: 24 }}>
                아직 등록된 리뷰가 없습니다. 첫 리뷰를 남겨보세요.
              </div>
            ) : (
              pagedReviews.map((review) => (
                <ReviewItem
                  key={review.id}
                  review={review}
                  currentUser={currentUser}
                  editingReviewId={editingReviewId}
                  actionLoading={reviewActionLoading}
                  recommendLoading={recommendLoadingId === review.id}
                  onStartEdit={setEditingReviewId}
                  onCancelEdit={() => setEditingReviewId(null)}
                  onSaveEdit={handleSaveEdit}
                  onDelete={handleDelete}
                  onRecommend={handleRecommend}
                  onOpenAuthorProfile={handleOpenAuthorProfile}
                />
              ))
            )}
          </div>
          {totalPages > 1 ? (
            <div
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: 8,
                flexWrap: "wrap",
                marginTop: 20,
                marginBottom: 28,
              }}
            >
              <button
                type="button"
                onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                disabled={safeCurrentPage === 1}
                style={{
                  padding: "8px 12px",
                  borderRadius: 10,
                  border: "1px solid #353a43",
                  background: "#171a20",
                  color: safeCurrentPage === 1 ? "#66707d" : "#d7dee7",
                  cursor: safeCurrentPage === 1 ? "not-allowed" : "pointer",
                }}
              >
                이전
              </button>
              {Array.from({ length: totalPages }, (_, index) => {
                const pageNumber = index + 1;
                const isActive = pageNumber === safeCurrentPage;

                return (
                  <button
                    key={pageNumber}
                    type="button"
                    onClick={() => setCurrentPage(pageNumber)}
                    style={{
                      minWidth: 40,
                      padding: "8px 12px",
                      borderRadius: 10,
                      border: isActive ? "1px solid #f6d365" : "1px solid #353a43",
                      background: isActive ? "#2d2614" : "#171a20",
                      color: isActive ? "#ffe29a" : "#d7dee7",
                      fontWeight: 700,
                      cursor: "pointer",
                    }}
                  >
                    {pageNumber}
                  </button>
                );
              })}
              <button
                type="button"
                onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
                disabled={safeCurrentPage === totalPages}
                style={{
                  padding: "8px 12px",
                  borderRadius: 10,
                  border: "1px solid #353a43",
                  background: "#171a20",
                  color: safeCurrentPage === totalPages ? "#66707d" : "#d7dee7",
                  cursor: safeCurrentPage === totalPages ? "not-allowed" : "pointer",
                }}
              >
                다음
              </button>
            </div>
          ) : null}
          <div style={{ marginTop: 32 }}>
            <ReviewForm currentUser={currentUser} onSubmit={handleReviewSubmit} submitting={submitting} />
          </div>
        </section>
      </div>
      {toast.message ? (
        <div
          style={{
            position: "fixed",
            left: "50%",
            bottom: 34,
            transform: toast.hiding ? "translateX(-50%) translateY(10px)" : "translateX(-50%) translateY(0)",
            padding: "13px 24px",
            borderRadius: 16,
            background: "rgba(24, 28, 34, 0.96)",
            color: "#f3f6fa",
            border: "1px solid rgba(255, 255, 255, 0.08)",
            boxShadow: "0 16px 36px rgba(0, 0, 0, 0.28)",
            fontWeight: 700,
            zIndex: 1000,
            opacity: toast.hiding ? 0 : 1,
            transition: "opacity 0.28s ease, transform 0.28s ease",
            pointerEvents: "none",
          }}
        >
          {toast.message}
        </div>
      ) : null}
    </div>
  );
}
