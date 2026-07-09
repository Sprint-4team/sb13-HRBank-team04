import { Outlet, useLocation } from "react-router-dom";
import Navigation from "@/components/layout/Navigation";
import { cx } from "@/utils/cx";
import Header from "../common/Header";

export default function Layout() {
  const { pathname } = useLocation();
  const isDashboard = pathname.startsWith("/dashboard");

  return (
    <div className="flex h-screen overflow-y-auto bg-white">
      {/* 사이드바 */}
      <Navigation />

      {/* 메인 컨텐츠 영역 */}
      <main
        className={cx(
          "flex min-w-0 flex-1 flex-col gap-7 px-[60px] pt-[100px] pb-[50px]",
          isDashboard && "overflow-y-auto bg-gray-50",
        )}
      >
        <Header />
        <div className="flex h-full min-h-0 min-w-0 flex-1 flex-col gap-4">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
