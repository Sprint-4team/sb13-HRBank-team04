// 부서 DTO
export interface DepartmentDto {
  id: number;
  name: string;
  description: string;
  establishedDate: string;
  employeeCount: number;
}

// 부서 목록 조회용 쿼리 타입
export interface DepartmentListQuery {
  nameOrDescription?: string;
  idAfter?: number;
  cursor?: string;
  size?: number;
  sortField?: "name" | "establishedDate";
  sortDirection?: "asc" | "desc";
}

// 부서 등록 요청
export interface DepartmentCreateRequest {
  name: string;
  description: string;
  establishedDate: string;
}

// 부서 수정 요청
export type DepartmentUpdateRequest = DepartmentCreateRequest;
