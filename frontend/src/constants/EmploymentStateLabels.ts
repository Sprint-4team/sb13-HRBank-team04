import type { EmployeeTrendUnit } from "@/model/employee";
import {
  EmploymentState,
  type EmploymentEnableStateType,
  type EmploymentStateType,
} from "@/types/enums";

export const EmploymentStateLabels: Record<EmploymentStateType, string> = {
  [EmploymentState.ALL]: "전체",
  [EmploymentState.ACTIVE]: "재직중",
  [EmploymentState.ON_LEAVE]: "휴직중",
  [EmploymentState.RESIGNED]: "퇴사",
};

export const EmploymentEnableStateLabels: Record<
  EmploymentEnableStateType,
  string
> = {
  [EmploymentState.ACTIVE]: "재직중",
  [EmploymentState.ON_LEAVE]: "휴직중",
  [EmploymentState.RESIGNED]: "퇴사",
};

export const EmployeeTrendFilterLabels: Record<EmployeeTrendUnit, string> = {
  day: "일별",
  week: "주별",
  month: "월별",
  quarter: "분기별",
  year: "연도별",
};
