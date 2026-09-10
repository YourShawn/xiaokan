import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchHistory, yuan } from "../api";
import type { Scan } from "../types";

function stamp(scan: Scan): string {
  if (scan.bought === true) return "拿下了";
  if (scan.bought === false) return "没买";
  return "还没砍";
}

export default function HistoryPage() {
  const [scans, setScans] = useState<Scan[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchHistory()
      .then(setScans)
      .catch((err: Error) => setError(err.message));
  }, []);

  return (
    <div className="page history">
      <p className="eyebrow">足迹</p>
      <h1>小砍看过的衣服</h1>
      {error && <p className="error">{error}</p>}
      {scans && scans.length === 0 && <p className="muted">还没有扫描。去拍一件吧。</p>}
      <ul className="history-list">
        {(scans ?? []).map((scan) => (
          <li key={scan.id}>
            <Link to={`/result/${scan.id}`}>
              <strong>{scan.brand ?? "未识别品牌"}</strong>
              <span>
                {scan.category ?? "衣服"} · 吊牌 {yuan(scan.list_price)}
              </span>
              <em>
                {stamp(scan)}
                {scan.bought === true && scan.saved_amount != null && scan.saved_amount > 0
                  ? ` · 省了 ¥${Number(scan.saved_amount).toFixed(0)}`
                  : ""}
              </em>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
}
