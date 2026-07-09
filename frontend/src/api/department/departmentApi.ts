import type {
  DepartmentCreateRequest,
  DepartmentDto,
  DepartmentListQuery,
  DepartmentUpdateRequest,
} from "@/model/department";
import type { CursorPageResponse } from "@/model/pagination";
import apiClient from "../client";

/**
 * 부서 목록 조회
 */
export function getDepartments(query: DepartmentListQuery): Promise<CursorPageResponse<DepartmentDto>> {
  return apiClient.get<CursorPageResponse<DepartmentDto>>("/departments", query);
}

/**
 * 부서 상세 조회
 */
export function getDepartmentById(id: number): Promise<DepartmentDto> {
  return apiClient.get<DepartmentDto>(`/departments/${id}`);
}

/**
 * 부서 등록
 */
export function createDepartment(request: DepartmentCreateRequest): Promise<DepartmentDto> {
  return apiClient.post<DepartmentDto>("/departments", request);
}

/**
 * 부서 수정
 */
export function updateDepartment(id: number, request: DepartmentUpdateRequest): Promise<DepartmentDto> {
  return apiClient.patch<DepartmentDto>(`/departments/${id}`, request);
}

/**
 * 부서 삭제
 */
export function deleteDepartment(id: number): Promise<void> {
  return apiClient.delete<void>(`/departments/${id}`);
}
