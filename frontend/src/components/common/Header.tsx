import { useLocation } from "react-router-dom";
import { NAV_ITEMS } from "@/constants/navigation";
import type { FC } from "react";

interface NavigationItem {
  label: string;
  path: string;
  icon: FC<{ className?: string }>;
}

const Header = () => {
  const { pathname } = useLocation();

  const currentMenu: NavigationItem | undefined = NAV_ITEMS.find((item) =>
    pathname.startsWith(item.path)
  );

  if (pathname.startsWith("/dashboard")) {
    return null;
  }

  const IconComponent = currentMenu?.icon;

  return (
    <header className="flex items-center gap-3 border-b pb-7 border-b-secondary">
      {IconComponent && (
        <div className="border border-gray-200 rounded-md p-2.5">
          <IconComponent className="size-5 text-gray-700" aria-hidden="true" />
        </div>
      )}
      <h1 className="text-display-sm font-bold text-secondary">
        {currentMenu?.label || "HR Bank"}
      </h1>
    </header>
  );
};

export default Header;
