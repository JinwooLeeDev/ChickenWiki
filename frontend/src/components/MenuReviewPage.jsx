import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Header from "./Header";
import {
  createMenuReview,
  deleteMenuReview,
  getMenu,
  getMenuReviews,
  updateMenuReview,
} from "../services/api";

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

  return `${value.toLocaleString()}\uC6D0`;
}

function ReviewItem({
  review,
  currentUser,
  editingReviewId,
  actionLoading,
  onStartEdit,
  onCancelEdit,
  onSaveEdit,
  onDelete,
}) {
  const isOwner = currentUser?.nickname === review.author;
  const isEditing = editingReviewId === review.id;
  const [content, setContent] = useState(review.content);
  const [rating, setRating] = useState(review.rating);

  useEffect(() => {
    setContent(review.content);
    setRating(review.rating);
  }, [review]);

  const handleSave = async () => {
    if (!content.trim()) {
      alert("리뷰 내용을 입력해주세요.");
      return;
    }

    await onSaveEdit(review.id, {
      content: content.trim(),
      rating,
    });
  };

  return (
    <div
      style={{
        background: "#1a1a1a",
        padding: 16,
        borderRadius: 8,
        marginBottom: 12,
        color: "white",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: 8,
          gap: 12,
        }}
      >
        <div style={{ fontWeight: 600 }}>{review.author}</div>
        <div style={{ color: "#ffd700" }}>
          {"\u2605".repeat(isEditing ? rating : review.rating)}
          {"\u2606".repeat(5 - (isEditing ? rating : review.rating))}
        </div>
      </div>

      {isEditing ? (
        <div style={{ display: "grid", gap: 12, marginBottom: 12 }}>
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
                {`${n}\uC810`}
              </option>
            ))}
          </select>
        </div>
      ) : (
        <div style={{ color: "#ccc", marginBottom: 12 }}>{review.content}</div>
      )}

      <div style={{ fontSize: 12, color: "#777" }}>{formatDate(review.createdAt)}</div>

      {isOwner ? (
        <div style={{ display: "flex", gap: 8, marginTop: 12, justifyContent: "flex-end" }}>
          {isEditing ? (
            <>
              <button
                type="button"
                onClick={handleSave}
                disabled={actionLoading}
                style={{
                  padding: "8px 12px",
                  borderRadius: 10,
                  border: "none",
                  background: "#f6d365",
                  color: "#17191d",
                  fontWeight: 700,
                  opacity: actionLoading ? 0.7 : 1,
                }}
              >
                {actionLoading ? "저장 중..." : "저장"}
              </button>
              <button
                type="button"
                onClick={onCancelEdit}
                disabled={actionLoading}
                style={{
                  padding: "8px 12px",
                  borderRadius: 10,
                  border: "1px solid #39414d",
                  background: "#181b21",
                  color: "white",
                }}
              >
                취소
              </button>
            </>
          ) : (
            <>
              <button
                type="button"
                onClick={() => onStartEdit(review.id)}
                disabled={actionLoading}
                style={{
                  padding: "8px 12px",
                  borderRadius: 10,
                  border: "1px solid #39414d",
                  background: "#181b21",
                  color: "white",
                }}
              >
                수정
              </button>
              <button
                type="button"
                onClick={() => onDelete(review.id)}
                disabled={actionLoading}
                style={{
                  padding: "8px 12px",
                  borderRadius: 10,
                  border: "1px solid #5a3038",
                  background: "#2b171b",
                  color: "#ffccd3",
                }}
              >
                삭제
              </button>
            </>
          )}
        </div>
      ) : null}
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
        <h3 style={{ marginTop: 0, marginBottom: 8 }}>{"\uB9AC\uBDF0 \uC791\uC131"}</h3>
        <div style={{ color: "#9aa6b2", lineHeight: 1.6, marginBottom: 16 }}>
          {"\uB9AC\uBDF0\uB97C \uB0A8\uAE30\uB824\uBA74 \uBA3C\uC800 \uB85C\uADF8\uC778\uD574\uC8FC\uC138\uC694."}
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
          {"\uB85C\uADF8\uC778\uD558\uB7EC \uAC00\uAE30"}
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
      <h3 style={{ color: "white", margin: 0 }}>{"\uB9AC\uBDF0 \uC791\uC131"}</h3>
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
          <div style={{ fontSize: 12, color: "#8f9aa7", marginBottom: 4 }}>{"\uC791\uC131\uC790"}</div>
          <div style={{ fontWeight: 700 }}>{currentUser.nickname}</div>
        </div>
        <div style={{ fontSize: 12, color: "#8f9aa7" }}>
          {"\uB2C9\uB124\uC784\uC740 \uACC4\uC815 \uC815\uBCF4\uC5D0\uC11C \uC790\uB3D9 \uC801\uC6A9\uB429\uB2C8\uB2E4."}
        </div>
      </div>
      <div style={{ display: "flex", gap: 16, alignItems: "flex-start", flexWrap: "wrap" }}>
        <textarea
          placeholder={"\uBA54\uB274\uC5D0 \uB300\uD55C \uC194\uC9C1\uD55C \uD6C4\uAE30\uB97C \uB0A8\uACA8\uC8FC\uC138\uC694"}
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
          <label style={{ color: "white", fontSize: 14, fontWeight: 700 }}>
            {"\uD3C9\uC810"}
          </label>
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
                {`${n}\uC810`}
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
        {submitting ? "\uB4F1\uB85D \uC911..." : "\uB9AC\uBDF0 \uB4F1\uB85D"}
      </button>
    </form>
  );
}

export default function MenuReviewPage() {
  const { id } = useParams();
  const menuId = parseInt(id, 10);
  const [menu, setMenu] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [reviewActionLoading, setReviewActionLoading] = useState(false);
  const [editingReviewId, setEditingReviewId] = useState(null);
  const [error, setError] = useState("");
  const [submitMessage, setSubmitMessage] = useState("");
  const [currentUser, setCurrentUser] = useState(null);

  useEffect(() => {
    try {
      const storedUser = localStorage.getItem("chickenwikiUser");
      setCurrentUser(storedUser ? JSON.parse(storedUser) : null);
    } catch (e) {
      console.error("Failed to read current user", e);
      setCurrentUser(null);
    }
  }, []);

  useEffect(() => {
    let mounted = true;

    async function loadMenuPage() {
      setLoading(true);
      setError("");
      setSubmitMessage("");

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
  }, [menuId]);

  const handleReviewSubmit = async (newReview) => {
    try {
      setSubmitting(true);
      setSubmitMessage("");
      const createdReview = await createMenuReview(menuId, newReview);
      setReviews((prev) => [createdReview, ...prev]);
      setSubmitMessage("\uB9AC\uBDF0\uAC00 \uB4F1\uB85D\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
      return true;
    } catch (e) {
      alert(e.message || "\uB9AC\uBDF0 \uB4F1\uB85D\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.");
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  const handleSaveEdit = async (reviewId, payload) => {
    try {
      setReviewActionLoading(true);
      setSubmitMessage("");
      const updatedReview = await updateMenuReview(menuId, reviewId, payload);
      setReviews((prev) => prev.map((review) => (review.id === reviewId ? updatedReview : review)));
      setEditingReviewId(null);
      setSubmitMessage("리뷰가 수정되었습니다.");
    } catch (e) {
      alert(e.message || "리뷰 수정에 실패했습니다.");
    } finally {
      setReviewActionLoading(false);
    }
  };

  const handleDelete = async (reviewId) => {
    if (!window.confirm("리뷰를 삭제하시겠습니까?")) {
      return;
    }

    try {
      setReviewActionLoading(true);
      setSubmitMessage("");
      await deleteMenuReview(menuId, reviewId);
      setReviews((prev) => prev.filter((review) => review.id !== reviewId));
      if (editingReviewId === reviewId) {
        setEditingReviewId(null);
      }
      setSubmitMessage("리뷰가 삭제되었습니다.");
    } catch (e) {
      alert(e.message || "리뷰 삭제에 실패했습니다.");
    } finally {
      setReviewActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={pageStyles.pageBackdrop}>
        <div style={{ ...pageStyles.contentWrap, color: "white" }}>
          <Header />
          <p>{"\uBA54\uB274 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4..."}</p>
        </div>
      </div>
    );
  }

  if (!menu || error === "menu-not-found") {
    return (
      <div style={pageStyles.pageBackdrop}>
        <div style={{ ...pageStyles.contentWrap, color: "white" }}>
          <Header />
          <p>{"\uBA54\uB274\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."}</p>
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
              {menu.description || "\uB4F1\uB85D\uB41C \uBA54\uB274 \uC124\uBA85\uC774 \uC5C6\uC2B5\uB2C8\uB2E4."}
            </p>
            <p style={{ fontSize: "1.2em", fontWeight: 600, color: "#ffd700" }}>
              {formatPrice(menu.menuPrice)}
            </p>
          </div>
        </div>
        </section>

        <section style={{ marginTop: 36 }}>
        <h2 style={{ marginBottom: 16 }}>{`\uB9AC\uBDF0 ${reviews.length}\uAC1C`}</h2>
        <ReviewForm currentUser={currentUser} onSubmit={handleReviewSubmit} submitting={submitting} />
        {submitMessage ? (
          <div
            style={{
              marginBottom: 16,
              padding: "12px 16px",
              borderRadius: 8,
              background: "#24311f",
              color: "#c9f5b0",
              border: "1px solid #3f5c33",
            }}
          >
            {submitMessage}
          </div>
        ) : null}
        <div>
          {reviews.length === 0 ? (
            <div style={{ color: "#9aa6b2", textAlign: "center", padding: 24 }}>
              {"\uC544\uC9C1 \uB4F1\uB85D\uB41C \uB9AC\uBDF0\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4. \uCCAB \uB9AC\uBDF0\uB97C \uB0A8\uACA8\uBCF4\uC138\uC694."}
            </div>
          ) : (
            reviews.map((review) => (
              <ReviewItem
                key={review.id}
                review={review}
                currentUser={currentUser}
                editingReviewId={editingReviewId}
                actionLoading={reviewActionLoading}
                onStartEdit={setEditingReviewId}
                onCancelEdit={() => setEditingReviewId(null)}
                onSaveEdit={handleSaveEdit}
                onDelete={handleDelete}
              />
            ))
          )}
        </div>
        </section>
      </div>
    </div>
  );
}
