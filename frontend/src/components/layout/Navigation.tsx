import type { FC } from "react";
import { Link, NavLink } from "react-router-dom";
import { NAV_ITEMS } from "@/constants/navigation";
import Logo from "@/assets/logo.ico";

interface NavigationItem {
  label: string;
  path: string;
  icon: FC<{ className?: string }>;
}

export default function Navigation() {
  return (
    <aside className="flex min-w-70 flex-col border-r border-gray-200 bg-gray-50 p-4">
      {/* 브랜드 영역 */}
      <div className="mt-6 mb-8 flex items-center gap-3 px-2">
        <Link to="/" aria-label="홈">
          <img
            src={Logo}
            alt="HR Bank"
            width={40}
            height={40}
            aria-hidden
          />
        </Link>
        <div className="flex flex-col">
          <span className="text-xl leading-none font-bold text-gray-900">
            HR Bank
          </span>
          <span className="text-xs leading-4 text-gray-500">
            인사관리시스템
          </span>
        </div>
      </div>

      {/* 네비게이션 섹션들 */}
      <nav className="flex-1">
        <ul className="space-y-2">
          {NAV_ITEMS.map((item: NavigationItem) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                end={item.path === "/dashboard"}
                className={({ isActive }) =>
                  [
                    "flex items-center gap-2 rounded-lg px-3 py-2 text-base transition",
                    isActive
                      ? "bg-brand-100 text-primary font-semibold"
                      : "text-quaternary font-normal hover:bg-gray-50",
                  ].join(" ")
                }
              >
                <item.icon className="size-5" aria-hidden="true" />
                <span className="truncate">{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
