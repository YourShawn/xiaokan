import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { fetchScan, recordDeal, yuan } from "../api";
import type { Scan } from "../types";

export default function DealPage() {
  const { id } = useParams();
  const [scan, setScan] = useState<Scan | null>(null);
  const [price, setPrice] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!id) return;
    fetchScan(id)
      .then(setScan)
      .catch((err: Error) => setError(err.message));
  }, [id]);

  async function submit(bought: boolean) {
    if (!id) return;
    setBusy(true);
    setError(null);
    try {
      const finalPrice = bought ? Number(price) : undefined;
      if (bought && (!price || Number.isNaN(finalPrice!) || finalPrice! < 0)) {
        setError("告诉小砍你最终付了多少");
        setBusy(false);
        return;
      }
      const next = await recordDeal(id, bought, finalPrice);
      setScan(next);
    } catch (err) {
      setError(err instanceof Error ? err.message : "记录失败");
    } finally {
      setBusy(false);
    }
  }

  if (!scan) {
    return (
      <div className="page">
        <p className="muted">{error ?? "带上小砍的纸条…"}</p>
      </div>
    );
  }

  if (scan.bought === true) {
    const saved = scan.saved_amount;
    return (
      <div className="page deal">
        <p className="eyebrow">拿下了</p>
        <h1>成交 {yuan(scan.final_price)}</h1>
        {saved != null && saved > 0 && <p className="saved">省了 ¥{Number(saved).toFixed(0)}</p>}
        {saved != null && saved <= 0 && <p>这次几乎没砍下来，下次开口再狠一点。</p>}
        {scan.list_price == null && <p className="muted">没有吊牌价，算不出省了多少。</p>}
        <Link className="cta" to="/history">
          看看足迹
        </Link>
      </div>
    );
  }

  if (scan.bought === false) {
    return (
      <div className="page deal">
        <p className="eyebrow">没买</p>
        <h1>那就不拿下。</h1>
        <p>小砍记着了。也许下一件更合适。</p>
        <Link className="cta" to="/history">
          看看足迹
        </Link>
      </div>
    );
  }

  return (
    <div className="page deal">
      <p className="eyebrow">口袋纸条</p>
      <h1>去砍价</h1>
      <div className="pocket-card">
        <p className="brand-line">{scan.brand ?? "这件"} · {scan.category ?? "衣服"}</p>
        <p>开口 {yuan(scan.opening_offer)}</p>
        <p>
          目标 {yuan(scan.target_min)} – {yuan(scan.target_max)}
        </p>
        <p>别超过 {yuan(scan.max_price)}</p>
      </div>
      <h2>拿下了吗？</h2>
      <label className="price-input">
        最终成交价
        <input
          inputMode="decimal"
          placeholder="例如 79"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
        />
      </label>
      {error && <p className="error">{error}</p>}
      <div className="row-actions">
        <button className="cta" type="button" disabled={busy} onClick={() => submit(true)}>
          拿下了
        </button>
        <button className="cta ghost" type="button" disabled={busy} onClick={() => submit(false)}>
          没买
        </button>
      </div>
    </div>
  );
}
