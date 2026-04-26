import React, { useEffect, useState } from "react";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import Header from "./Header";
import { deleteAdminUserByNickname, getAdminUserByNickname } from "../services/api";

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

function readStoredUser() {
  try {
    const storedUser = localStorage.getItem("chickenwikiUser");
    return storedUser ? JSON.parse(storedUser) : null;
  } catch (e) {
    console.error("Failed to read current user", e);
    return null;
  }
}

function formatDate(value) {
  if (!value) return "-";

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

function renderStars(rating) {
  return `${"★".repeat(rating)}${"☆".repeat(5 - rating)}`;
}

export default function AdminUserDetailPage() {
  const { nickname } = useParams();
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState(() => readStoredUser());
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const syncUser = () => {
      setCurrentUser(readStoredUser());
    };

    window.addEventListener("storage", syncUser);
    window.addEventListener("chickenwiki-auth-changed", syncUser);

    return () => {
      window.removeEventListener("storage", syncUser);
      window.removeEventListener("chickenwiki-auth-changed", syncUser);
    };
  }, []);

  useEffect(() => {
    if (!currentUser?.token || currentUser?.role !== "ADMIN" || !nickname) {
      setLoading(false);
      return;
    }

    let mounted = true;

    async function loadProfile() {
      try {
        setLoading(true);
        setError("");
        const data = await getAdminUserByNickname(nickname);

        if (!mounted) return;
        setProfile(data);
      } catch (e) {
        if (!mounted) return;
        setProfile(null);
        setError(e.message || "사용자 정보를 불러오지 못했습니다.");
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    }

    loadProfile();

    return () => {
      mounted = false;
    };
  }, [currentUser, nickname]);

  const handleDeleteAccount = async () => {
    if (!profile) return;

    const confirmed = window.confirm(
      "이 사용자의 계정을 삭제할까요?\n\n계정 정보, 작성한 모든 댓글, 남긴 추천이 함께 삭제됩니다."
    );

    if (!confirmed) {
      return;
    }

    try {
      setDeleting(true);
      await deleteAdminUserByNickname(profile.nickname);
      alert("계정을 삭제했습니다.");
      navigate(-1);
    } catch (e) {
      alert(e.message || "계정 삭제에 실패했습니다.");
    } finally {
      setDeleting(false);
    }
  };

  if (!currentUser && !loading) {
    return <Navigate to="/login" replace />;
  }

  if (currentUser && currentUser.role !== "ADMIN" && !loading) {
    return <Navigate to="/" replace />;
  }

  return (
    <div style={pageStyles.pageBackdrop}>
      <div style={pageStyles.contentWrap}>
        <Header />

        <section
          style={{
            marginTop: 24,
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
            gap: 20,
          }}
        >
          <div
            style={{
              padding: 24,
              borderRadius: 20,
              background: "linear-gradient(180deg, #1d2026 0%, #13161b 100%)",
              border: "1px solid #2b313a",
              boxShadow: "0 18px 36px rgba(0, 0, 0, 0.2)",
            }}
          >
            <div style={{ color: "#f6d365", fontWeight: 700, marginBottom: 12 }}>관리 대상 사용자</div>
            {loading ? (
              <div style={{ color: "#9aa6b2" }}>사용자 정보를 불러오는 중입니다...</div>
            ) : error ? (
              <div style={{ color: "#ffccd3" }}>{error}</div>
            ) : profile ? (
              <div style={{ display: "grid", gap: 14 }}>
                <div>
                  <div style={{ color: "#8e99a8", fontSize: 13, marginBottom: 4 }}>닉네임</div>
                  <div style={{ color: "white", fontWeight: 700, fontSize: 20 }}>{profile.nickname}</div>
                </div>
                <div>
                  <div style={{ color: "#8e99a8", fontSize: 13, marginBottom: 4 }}>아이디</div>
                  <div style={{ color: "#dfe6ee" }}>{profile.username}</div>
                </div>
                <div>
                  <div style={{ color: "#8e99a8", fontSize: 13, marginBottom: 4 }}>가입일</div>
                  <div style={{ color: "#dfe6ee" }}>{formatDate(profile.createdAt)}</div>
                </div>
              </div>
            ) : null}
          </div>

          <div
            style={{
              padding: 24,
              borderRadius: 20,
              background: "linear-gradient(180deg, #23191b 0%, #181113 100%)",
              border: "1px solid #55363b",
              boxShadow: "0 18px 36px rgba(0, 0, 0, 0.2)",
            }}
          >
            <div style={{ color: "#ffd5db", fontWeight: 700, marginBottom: 12 }}>관리자 작업</div>
            <div style={{ color: "#e8c9ce", lineHeight: 1.7, marginBottom: 18 }}>
              계정 삭제를 실행하면 이 사용자의 계정 정보, 작성한 댓글, 남긴 추천이 함께 삭제됩니다.
            </div>
            <button
              type="button"
              onClick={handleDeleteAccount}
              disabled={deleting || loading || !profile}
              style={{
                padding: "12px 18px",
                borderRadius: 12,
                border: "1px solid #7a424c",
                background: "#432027",
                color: "#ffd9de",
                fontWeight: 700,
                cursor: deleting || loading || !profile ? "not-allowed" : "pointer",
                opacity: deleting || loading || !profile ? 0.7 : 1,
              }}
            >
              {deleting ? "삭제 중..." : "계정 삭제"}
            </button>
          </div>
        </section>

        <section
          style={{
            marginTop: 28,
            padding: 24,
            borderRadius: 20,
            background: "#171a20",
            border: "1px solid #2a2f38",
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              gap: 12,
              flexWrap: "wrap",
              marginBottom: 18,
            }}
          >
            <h2 style={{ margin: 0, color: "white" }}>작성한 모든 댓글</h2>
            {profile ? (
              <div style={{ color: "#9aa6b2" }}>{`총 ${profile.reviews?.length || 0}개`}</div>
            ) : null}
          </div>

          {loading ? (
            <div style={{ color: "#9aa6b2" }}>댓글 목록을 불러오는 중입니다...</div>
          ) : error ? (
            <div
              style={{
                padding: 16,
                borderRadius: 14,
                background: "#2a1d20",
                border: "1px solid #57333a",
                color: "#ffccd3",
              }}
            >
              {error}
            </div>
          ) : !profile?.reviews?.length ? (
            <div
              style={{
                padding: 24,
                borderRadius: 16,
                background: "#11141a",
                border: "1px solid #252a33",
                color: "#9aa6b2",
                lineHeight: 1.7,
              }}
            >
              작성한 댓글이 없습니다.
            </div>
          ) : (
            <div style={{ display: "grid", gap: 14 }}>
              {profile.reviews.map((review) => (
                <Link
                  key={review.id}
                  to={`/menu/${review.menuId}`}
                  style={{
                    display: "flex",
                    gap: 16,
                    alignItems: "stretch",
                    flexWrap: "wrap",
                    padding: 18,
                    borderRadius: 16,
                    background: "#101318",
                    border: "1px solid #262c35",
                    textDecoration: "none",
                    color: "inherit",
                  }}
                >
                  <div
                    style={{
                      width: 96,
                      height: 96,
                      borderRadius: 14,
                      overflow: "hidden",
                      background: "#1b1f26",
                      flex: "0 0 auto",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: "#697382",
                      fontSize: 12,
                    }}
                  >
                    {review.menuImageUrl ? (
                      <img
                        src={review.menuImageUrl}
                        alt={review.menuName}
                        style={{ width: "100%", height: "100%", objectFit: "cover" }}
                      />
                    ) : (
                      "NO IMAGE"
                    )}
                  </div>

                  <div style={{ flex: 1, minWidth: 220 }}>
                    <div style={{ color: "#8e99a8", fontSize: 13, marginBottom: 6 }}>{review.brandName}</div>
                    <div style={{ color: "white", fontWeight: 700, fontSize: 18, marginBottom: 8 }}>
                      {review.menuName}
                    </div>
                    <div style={{ color: "#ffd86f", marginBottom: 8 }}>{renderStars(review.rating)}</div>
                    <div style={{ color: "#d6dde5", lineHeight: 1.6 }}>{review.content}</div>
                  </div>

                  <div
                    style={{
                      color: "#7f8a97",
                      fontSize: 13,
                      alignSelf: "flex-start",
                      marginLeft: "auto",
                    }}
                  >
                    {formatDate(review.createdAt)}
                  </div>
                </Link>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
