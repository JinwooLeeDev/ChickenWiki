import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import Header from "./Header";
import { createMenuReview, getMenu, getMenuReviews } from "../services/api";

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

function ReviewItem({ review }) {
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
          {"\u2605".repeat(review.rating)}
          {"\u2606".repeat(5 - review.rating)}
        </div>
      </div>
      <div style={{ color: "#ccc", marginBottom: 12 }}>{review.content}</div>
      <div style={{ fontSize: 12, color: "#777" }}>{formatDate(review.createdAt)}</div>
    </div>
  );
}

function ReviewForm({ onSubmit, submitting }) {
  const [author, setAuthor] = useState("");
  const [content, setContent] = useState("");
  const [rating, setRating] = useState(5);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) return;

    const success = await onSubmit({
      author: author.trim(),
      content: content.trim(),
      rating,
    });

    if (success) {
      setAuthor("");
      setContent("");
      setRating(5);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      style={{
        background: "#1a1a1a",
        padding: 20,
        borderRadius: 8,
        marginBottom: 24,
        display: "flex",
        flexDirection: "column",
        gap: 16,
      }}
    >
      <h3 style={{ color: "white", margin: 0 }}>{"\uB9AC\uBDF0 \uC791\uC131"}</h3>
      <input
        placeholder={"\uB2C9\uB124\uC784\uC744 \uC785\uB825\uD558\uC138\uC694"}
        value={author}
        onChange={(e) => setAuthor(e.target.value)}
        style={{
          padding: "12px",
          borderRadius: 6,
          border: "1px solid #333",
          background: "#0f0f0f",
          color: "white",
          fontSize: 14,
        }}
      />
      <div style={{ display: "flex", gap: 16, alignItems: "flex-start", flexWrap: "wrap" }}>
        <textarea
          placeholder={"\uBA54\uB274\uC5D0 \uB300\uD55C \uC194\uC9C1\uD55C \uD6C4\uAE30\uB97C \uB0A8\uACA8\uC8FC\uC138\uC694"}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
          style={{
            flex: 1,
            minWidth: 260,
            padding: "12px",
            borderRadius: 6,
            border: "1px solid #333",
            background: "#0f0f0f",
            color: "white",
            resize: "vertical",
            fontSize: 14,
          }}
        />
        <div style={{ display: "flex", flexDirection: "column", gap: 8, minWidth: 120 }}>
          <label style={{ color: "white", fontSize: 14 }}>{"\uD3C9\uC810"}</label>
          <select
            value={rating}
            onChange={(e) => setRating(parseInt(e.target.value, 10))}
            style={{
              padding: "8px 12px",
              borderRadius: 6,
              border: "1px solid #333",
              background: "#0f0f0f",
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
          padding: "10px 20px",
          background: "#ffd700",
          color: "black",
          border: "none",
          borderRadius: 6,
          cursor: submitting ? "not-allowed" : "pointer",
          fontWeight: 600,
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
  const [error, setError] = useState("");
  const [submitMessage, setSubmitMessage] = useState("");

  useEffect(() => {
    let mounted = true;

    async function loadMenuPage() {
      setLoading(true);
      setError("");
      setSubmitMessage("");

      try {
        const [menuData, reviewData] = await Promise.all([
          getMenu(menuId),
          getMenuReviews(menuId),
        ]);

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
      setSubmitMessage(
        "\uB9AC\uBDF0\uAC00 \uB4F1\uB85D\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uC0C8\uB85C\uACE0\uCE68\uD558\uC9C0 \uC54A\uC544\uB3C4 \uBC14\uB85C \uBC18\uC601\uB3FC\uC694."
      );
      return true;
    } catch (e) {
      alert(
        "\uB9AC\uBDF0 \uB4F1\uB85D\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4. \uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694."
      );
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto", color: "white" }}>
        <Header />
        <p>{"\uBA54\uB274 \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4..."}</p>
      </div>
    );
  }

  if (!menu || error === "menu-not-found") {
    return (
      <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto", color: "white" }}>
        <Header />
        <p>{"\uBA54\uB274\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4."}</p>
      </div>
    );
  }

  return (
    <div style={{ padding: 28, maxWidth: 1100, margin: "0 auto" }}>
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
        <ReviewForm onSubmit={handleReviewSubmit} submitting={submitting} />
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
            reviews.map((review) => <ReviewItem key={review.id} review={review} />)
          )}
        </div>
      </section>
    </div>
  );
}
