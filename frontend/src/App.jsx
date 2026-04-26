import { Routes, Route } from "react-router-dom";
import MainPage from "./components/MainPage";
import BrandsPage from "./components/BrandsPage";
import LoginPage from "./components/LoginPage";
import BrandDetailPage from "./components/BrandDetailPage";
import MenuReviewPage from "./components/MenuReviewPage";
import MyPage from "./components/MyPage";
import AdminUserDetailPage from "./components/AdminUserDetailPage";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<MainPage />} />
      <Route path="/brands" element={<BrandsPage />} />
      <Route path="/brand/:id" element={<BrandDetailPage />} />
      <Route path="/menu/:id" element={<MenuReviewPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/mypage" element={<MyPage />} />
      <Route path="/admin/users/:nickname" element={<AdminUserDetailPage />} />
    </Routes>
  );
}
