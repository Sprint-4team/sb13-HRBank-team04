import type {
  EmployeeCountQuery,
  EmployeeCreateRequest,
  EmployeeDistributionDto,
  EmployeeDistributionQuery,
  EmployeeDto,
  EmployeeListQuery,
  EmployeeTrendDto,
  EmployeeTrendQuery,
  EmployeeUpdateRequest,
} from "@/model/employee";
import type { CursorPageResponse } from "@/model/pagination";
import { EmploymentState } from "@/types/enums";
import apiClient from "../client";

/**
 * 직원 목록 조회
 */
export function getEmployees(
  query: EmployeeListQuery,
): Promise<CursorPageResponse<EmployeeDto>> {
  if (query.status === EmploymentState.ALL) {
    query.status = "";
  }
  return apiClient.get<CursorPageResponse<EmployeeDto>>("/employees", query);
}

/**
 * 직원 상세 조회
 */
export function getEmployeeById(id: number): Promise<EmployeeDto> {
  return apiClient.get<EmployeeDto>(`/employees/${id}`);
}

/**
 * 직원 등록
 */
export function createEmployee(
  request: EmployeeCreateRequest,
  profileFile?: File | null,
): Promise<EmployeeDto> {
  const formData = new FormData();
  formData.append(
    "employee",
    new Blob([JSON.stringify(request)], { type: "application/json" }),
  );
  if (profileFile) {
    formData.append("profile", profileFile);
  }

  return apiClient.multiPartPost<EmployeeDto>("/employees", formData);
}

/**
 * 직원 수정
 */
export function updateEmployee(
  id: number,
  request: EmployeeUpdateRequest,
  profileFile?: File | null,
): Promise<EmployeeDto> {
  const formData = new FormData();
  formData.append(
    "employee",
    new Blob([JSON.stringify(request)], { type: "application/json" }),
  );

  if (profileFile !== undefined && profileFile !== null) {
    formData.append("profile", profileFile);
  }

  return apiClient.multiPartPatch<EmployeeDto>(`/employees/${id}`, formData);
}

/**
 * 직원 삭제
 */
export function deleteEmployee(id: number): Promise<void> {
  return apiClient.delete<void>(`/employees/${id}`);
}

/**
 * 직원 수 추이 조회
 */
export function getEmployeeTrend(
  query?: EmployeeTrendQuery,
): Promise<EmployeeTrendDto[]> {
  return apiClient.get<EmployeeTrendDto[]>("/employees/stats/trend", query);
}

/**
 * 직원 분포 조회 (부서별/직무별)
 */
export function getEmployeeDistribution(
  query?: EmployeeDistributionQuery,
): Promise<EmployeeDistributionDto[]> {
  return apiClient.get<EmployeeDistributionDto[]>(
    "/employees/stats/distribution",
    query,
  );
}

/**
 * 직원 수 조회
 */
export function getEmployeeCount(query?: EmployeeCountQuery): Promise<number> {
  return apiClient.get<number>("/employees/count", query);
}
