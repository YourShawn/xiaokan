import { Link, NavLink } from "react-router-dom";
import type { ReactNode } from "react";

export default function Shell({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="brand">
          <span className="brand-mark">砍</span>
          小砍
        </Link>
        <nav>
          <NavLink to="/" end>
            拍一拍
          </NavLink>
          <NavLink to="/history">足迹</NavLink>
        </nav>
      </header>
      <main>{children}</main>
    </div>
  );
}
