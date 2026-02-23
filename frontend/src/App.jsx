import { useEffect, useState } from "react";

export default function App() {
  const [text, setText] = useState("loading...");

  useEffect(() => {
    fetch("/api/health")
      .then((res) => res.text())
      .then(setText)
      .catch((e) => setText("API 호출 실패: " + e.message));
  }, []);

  return (
    <div style={{ padding: 24 }}>
      <h1>ChickinWiki</h1>
      <p>Backend says: {text}</p>
    </div>
  );
}
