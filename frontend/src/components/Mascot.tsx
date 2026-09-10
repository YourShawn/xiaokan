type Props = { size?: number; wave?: boolean };

export default function Mascot({ size = 140, wave = true }: Props) {
  return (
    <svg
      className={wave ? "mascot-svg mascot-wave" : "mascot-svg"}
      width={size}
      height={size}
      viewBox="0 0 160 160"
      aria-hidden="true"
    >
      <ellipse cx="80" cy="138" rx="42" ry="8" fill="#e8c4a8" opacity="0.45" />
      <path
        d="M80 28c28 0 52 22 52 54 0 36-24 58-52 58S28 118 28 82c0-32 24-54 52-54z"
        fill="#ffd0b5"
      />
      <path d="M80 28c8-16 28-18 34-8 2 4-4 10-14 14-8 3-16 4-20-6z" fill="#8fbf7a" />
      <circle cx="62" cy="78" r="6.5" fill="#4a3428" />
      <circle cx="98" cy="78" r="6.5" fill="#4a3428" />
      <circle cx="64" cy="76" r="2" fill="#fff" />
      <circle cx="100" cy="76" r="2" fill="#fff" />
      <path d="M70 98c6 8 14 8 20 0" fill="none" stroke="#c96b6b" strokeWidth="3" strokeLinecap="round" />
      <ellipse cx="50" cy="92" rx="8" ry="5" fill="#ffb3c0" opacity="0.85" />
      <ellipse cx="110" cy="92" rx="8" ry="5" fill="#ffb3c0" opacity="0.85" />
      <g transform="translate(112 86) rotate(18)">
        <rect x="0" y="10" width="8" height="28" rx="2" fill="#c47a4a" />
        <path d="M-6 10h20l-4 14h-12z" fill="#f2f0ea" stroke="#c47a4a" />
        <text x="4" y="21" textAnchor="middle" fontSize="8" fill="#c47a4a">
          ¥
        </text>
      </g>
    </svg>
  );
}
