import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createScan, fetchStatus } from "../api";
import Mascot from "../components/Mascot";

type Slot = {
  key: string;
  title: string;
  hint: string;
  file: File | null;
  preview: string | null;
};

const EMPTY_SLOTS: Slot[] = [
  { key: "garment", title: "衣服", hint: "正面全身或领口", file: null, preview: null },
  { key: "tag", title: "价签", hint: "拍清楚金额", file: null, preview: null },
  { key: "care", title: "水洗标", hint: "面料成分", file: null, preview: null }
];

export default function HomePage() {
  const navigate = useNavigate();
  const [slots, setSlots] = useState<Slot[]>(EMPTY_SLOTS);
  const [mock, setMock] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const captureRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchStatus()
      .then((s) => setMock(s.mock_mode))
      .catch(() => setMock(true));
  }, []);

  const files = useMemo(() => slots.filter((s) => s.file).map((s) => s.file!) , [slots]);
  const skippedTag = !slots[1].file && files.length > 0;

  function onPick(index: number, fileList: FileList | null) {
    const file = fileList?.[0];
    if (!file) return;
    setError(null);
    const preview = URL.createObjectURL(file);
    setSlots((prev) =>
      prev.map((slot, i) => (i === index ? { ...slot, file, preview } : slot))
    );
  }

  function clearSlot(index: number) {
    setSlots((prev) =>
      prev.map((slot, i) => (i === index ? { ...slot, file: null, preview: null } : slot))
    );
  }

  async function submit() {
    if (files.length === 0) {
      setError("先给小砍看一眼衣服呀");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const scan = await createScan(files);
      navigate(`/result/${scan.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "提交失败");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="page home">
      <section className="hero">
        <Mascot />
        <p className="eyebrow">{mock ? "模拟模式 · 不必连视觉模型" : "视觉已接通"}</p>
        <h1>拍一下，看看这件衣服多少钱值得拿下。</h1>
        <button
          className="cta"
          type="button"
          onClick={() => captureRef.current?.scrollIntoView({ behavior: "smooth" })}
        >
          这件衣服，能砍到多少？
        </button>
      </section>

      <section className="capture" ref={captureRef}>
        <p className="section-kicker">拍 1～3 张 · 衣服 / 价签 / 水洗标</p>
        <div className="slots">
          {slots.map((slot, index) => (
            <label key={slot.key} className={`slot ${slot.preview ? "filled" : ""}`}>
              {slot.preview ? (
                <>
                  <img src={slot.preview} alt={slot.title} />
                  <button
                    type="button"
                    className="slot-clear"
                    onClick={(e) => {
                      e.preventDefault();
                      clearSlot(index);
                    }}
                  >
                    重拍
                  </button>
                </>
              ) : (
                <span className="slot-empty">
                  <strong>{slot.title}</strong>
                  <em>{slot.hint}</em>
                  <span className="plus">+</span>
                </span>
              )}
              <input
                type="file"
                accept="image/*"
                capture="environment"
                onChange={(e) => onPick(index, e.target.files)}
              />
            </label>
          ))}
        </div>
        {skippedTag && (
          <p className="soft-tip">价签再拍一张会更准哦，也可以先跳过。</p>
        )}
        {error && <p className="error">{error}</p>}
        <button className="cta secondary" type="button" disabled={busy} onClick={submit}>
          {busy ? "小砍正在看…" : "看看小砍怎么说"}
        </button>
      </section>
    </div>
  );
}
