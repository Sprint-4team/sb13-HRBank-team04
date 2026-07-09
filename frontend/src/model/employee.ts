import type { EmployeeStatus } from "@/types/enums";

// 직원 DTO
export interface EmployeeDto {
  id: number;
  name: string;
  email: string;
  employeeNumber: string;
  departmentId: number;
  departmentName: string;
  position: string;
  hireDate: string; // "YYYY-MM-DD"
  status: EmployeeStatus;
  memo?: string;
  profileImageId: number | null;
}

// 직원 목록 조회용 쿼리 타입
export interface EmployeeListQuery {
  nameOrEmail?: string;
  employeeNumber?: string;
  departmentName?: string;
  position?: string;
  hireDateFrom?: string;
  hireDateTo?: string;
  status?: EmployeeStatus | "";
  idAfter?: number;
  cursor?: string;
  size?: number;
  sortField?: "name" | "employeeNumber" | "hireDate";
  sortDirection?: "asc" | "desc";
}

// 직원 등록 요청
export interface EmployeeCreateRequest {
  name: string;
  email: string;
  departmentId: number;
  position: string;
  hireDate: string;
  memo?: string;
}

// 직원 수정 요청
export interface EmployeeUpdateRequest {
  name: string;
  email: string;
  departmentId: number;
  position: string;
  hireDate: string;
  status: string;
  memo?: string;
}

// 직원 수 추이 단위
export type EmployeeTrendUnit = "day" | "week" | "month" | "quarter" | "year";

// 직원 수 추이 DTO
export interface EmployeeTrendDto {
  date: string;
  count: number;
  change: number;
  changeRate: number;
}

export type EmployeeDistributionGroupBy = "department" | "position";

// 직원 분포 DTO
export interface EmployeeDistributionDto {
  groupKey: string;
  count: number;
  percentage: number;
}

export interface EmployeeTrendQuery {
  from?: string;
  to?: string;
  unit?: EmployeeTrendUnit;
}

export interface EmployeeDistributionQuery {
  groupBy?: EmployeeDistributionGroupBy;
  status?: EmployeeStatus;
}

export interface EmployeeCountQuery {
  status?: EmployeeStatus;
  fromDate?: string;
  toDate?: string;
}
