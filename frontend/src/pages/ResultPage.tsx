import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { fetchScan, photoUrl, yuan } from "../api";
import type { Scan } from "../types";

export default function ResultPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [scan, setScan] = useState<Scan | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    fetchScan(id)
      .then(setScan)
      .catch((err: Error) => setError(err.message));
  }, [id]);

  if (error) {
    return (
      <div className="page">
        <p className="error">{error}</p>
        <Link to="/">回去再拍</Link>
      </div>
    );
  }
  if (!scan) {
    return (
      <div className="page">
        <p className="muted">小砍翻相册中…</p>
      </div>
    );
  }

  if (scan.insufficient_evidence) {
    return (
      <div className="page result">
        <p className="eyebrow">没看清</p>
        <h1>这件衣服，小砍还看不太懂。</h1>
        <p>{scan.retake_hint ?? "再靠近一点拍衣服正面吧。"}</p>
        <div className="photo-row">
          {scan.photos.map((p) => (
            <img key={p.id} src={photoUrl(p.url)} alt="" />
          ))}
        </div>
        <Link className="cta" to="/">
          再拍一张
        </Link>
      </div>
    );
  }

  return (
    <div className="page result">
      {scan.mock_mode && <p className="pill">模拟建议 · 不是店内实价</p>}
      <p className="eyebrow">{scan.category ?? "衣服"}</p>
      <h1>{scan.brand ?? "品牌未识别"}</h1>
      <dl className="facts">
        <div>
          <dt>吊牌价</dt>
          <dd>{yuan(scan.list_price)}</dd>
        </div>
        <div>
          <dt>面料</dt>
          <dd>{scan.fabric ?? "—"}</dd>
        </div>
        {scan.sku && (
          <div>
            <dt>货号</dt>
            <dd>{scan.sku}</dd>
          </div>
        )}
      </dl>

      {scan.needs_retake_tag && (
        <p className="soft-tip">
          {scan.retake_hint ?? "价签再拍一张会更准哦，也可以先跳过。"}{" "}
          <Link to="/">去补拍</Link>
        </p>
      )}

      <section className="advice">
        <h2>小砍建议</h2>
        <div className="advice-grid">
          <article>
            <span>开口</span>
            <strong>{yuan(scan.opening_offer)}</strong>
          </article>
          <article>
            <span>目标</span>
            <strong>
              {yuan(scan.target_min)}
              <em> – </em>
              {yuan(scan.target_max)}
            </strong>
          </article>
          <article>
            <span>心理上限</span>
            <strong>{yuan(scan.max_price)}</strong>
          </article>
          <article className="score">
            <span>砍价指数</span>
            <strong>{scan.bargain_score ?? "—"}</strong>
          </article>
        </div>
        <ul className="reasons">
          {(scan.reasons ?? []).map((reason) => (
            <li key={reason}>{reason}</li>
          ))}
        </ul>
      </section>

      <div className="photo-row">
        {scan.photos.map((p) => (
          <img key={p.id} src={photoUrl(p.url)} alt="" />
        ))}
      </div>

      <button className="cta" type="button" onClick={() => navigate(`/deal/${scan.id}`)}>
        去砍价
      </button>
    </div>
  );
}
