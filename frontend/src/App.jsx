import { Routes, Route } from "react-router-dom";
import MainPage from "./components/MainPage";
import BrandsPage from "./components/BrandsPage";
import LoginPage from "./components/LoginPage";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<MainPage />} />
      <Route path="/brands" element={<BrandsPage />} />
      <Route path="/login" element={<LoginPage />} />
    </Routes>
  );
}
