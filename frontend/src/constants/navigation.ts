import {
  Building05,
  ClockFastForward,
  Database01,
  LineChartUp02,
  Users01,
} from "@untitledui/icons";

export const NAV_ITEMS = [
  {
    label: "대시보드",
    path: "/dashboard",
    icon: LineChartUp02,
  },
  {
    label: "부서 관리",
    path: "/departments",
    icon: Building05,
  },
  {
    label: "직원 관리",
    path: "/employees",
    icon: Users01,
  },
  {
    label: "수정 이력",
    path: "/histories",
    icon: ClockFastForward,
  },
  {
    label: "데이터 백업",
    path: "/backups",
    icon: Database01,
  },
];
