import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { Menu, X, Search, Accessibility, ExternalLink } from "lucide-react";
import { NAV_LINKS } from "../utils/constants.js";
import { useAuth } from "../hooks/useAuth.js";
import logo from "../ChatGPT.png";

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const links = user?.role === "district_officer"
    ? [...NAV_LINKS, { label: "Approvals", to: "/admin" }]
    : NAV_LINKS;
  const mobileLinks = [
    { label: "Home", to: "/" },
    { label: "Map", to: "/dashboard" },
    { label: "Check Risk", to: "/dashboard/risk" },
    { label: "Report Incident", to: "/report-incident" },
    { label: "Account", to: "/account" },
    ...(user?.role === "district_officer" ? [{ label: "Approvals", to: "/admin" }] : []),
  ];

  return (
    <header className="sticky top-0 z-40 bg-white shadow-sm">
      <div className="border-b border-[#dbe5ea] bg-[#f5f8fa] text-xs text-[#526579]">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-2 sm:px-6 lg:px-8">
          <span>Government of India | North Eastern Region</span>
          <div className="hidden items-center gap-4 sm:flex"><span>Skip to main content</span><span>A+</span><Accessibility className="h-3.5 w-3.5" /></div>
        </div>
      </div>
      <div className="border-b border-[#dbe5ea] bg-white">
        <nav className="mx-auto flex max-w-7xl items-center justify-between gap-6 px-4 py-4 sm:px-6 lg:px-8">
          <Link to="/" className="flex shrink-0 items-center gap-3">
            <img src={logo} alt="PahadSuraksha logo" className="h-12 w-12 rounded object-cover" />
            <span className="border-l border-slate-200 pl-3">
              <span className="block font-display text-xl font-bold leading-tight text-[#102a43]">PahadSuraksha AI</span>
              <span className="block text-xs font-semibold uppercase tracking-[0.1em] text-[#087f8c]">Regional early warning network</span>
            </span>
          </Link>
          <div className="hidden items-center gap-5 text-right md:block">
            <p className="text-xs font-semibold uppercase tracking-[0.12em] text-[#526579]">Landslide risk monitoring</p>
            <p className="text-sm text-[#102a43]">Protecting communities across the North East</p>
          </div>
        </nav>
      </div>

      <nav className="border-b border-[#0b5266] bg-[#0b5266]">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        <div className="hidden items-center gap-1 md:flex">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              className={({ isActive }) =>
                `px-4 py-3 text-sm transition-colors duration-200 ${
                  isActive ? "bg-white/15 font-semibold text-white" : "text-white/90 hover:bg-white/10 hover:text-white"
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </div>

        <div className="hidden items-center gap-3 md:flex">
          {user ? (
            <>
              <span className="text-sm text-white">{user.name}</span>
              <button
                onClick={() => {
                  logout();
                  navigate("/");
                }}
                className="btn-secondary text-sm"
              >
                Log out
              </button>
            </>
          ) : (
            <Link to="/login" className="border border-white/50 px-3 py-1.5 text-sm font-semibold text-white transition-colors hover:bg-white hover:text-[#0b5266]">
              Login
            </Link>
          )}
        </div>

        <div className="flex items-center gap-3 md:hidden">
          <Search className="h-5 w-5 text-white" />
          <button className="text-white" onClick={() => setOpen((o) => !o)} aria-label="Toggle menu">
            {open ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
          </button>
        </div>
        </div>
      </nav>
      <div className="hidden border-b border-[#dbe5ea] bg-[#f5f8fa] lg:block">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-2 text-xs text-[#526579] sm:px-6 lg:px-8">
          <span>Emergency response information and verified field reports</span>
          <span className="inline-flex items-center gap-1 text-[#0b5266]"><ExternalLink className="h-3 w-3" /> Official monitoring service</span>
        </div>
      </div>

      {open && (
        <div className="space-y-2 border-t border-slate-200 bg-white px-4 py-4 md:hidden">
          {mobileLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              onClick={() => setOpen(false)}
              className={({ isActive }) =>
                `block px-3 py-2.5 rounded-md text-sm transition-colors duration-200 ${
                  isActive ? "bg-[#e7f4f5] font-semibold text-[#0b5266]" : "text-slate-600 hover:bg-slate-50 hover:text-[#102a43]"
                }`
              }
            >
              {link.label}
            </NavLink>
          ))}
          <div className="border-t border-slate-200 pt-2">
            {user ? (
              <button
                onClick={() => {
                  logout();
                  setOpen(false);
                  navigate("/");
                }}
                className="btn-secondary w-full text-sm mt-2"
              >
                Log out
              </button>
            ) : (
              <Link to="/login" onClick={() => setOpen(false)} className="btn-primary w-full text-sm block text-center">
                Login
              </Link>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
