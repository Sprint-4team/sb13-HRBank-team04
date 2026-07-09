import type { HistoryType } from "@/types/enums";

// 수정이력 DTO
export interface HistoryDto {
  id: number;
  type: HistoryType;
  employeeNumber: string;
  memo: string | null;
  ipAddress: string;
  at: string;
  employeeName?: string;
  profileImageId?: number | null;
  diffs?: Array<{
    propertyName: string;
    before: string | null;
    after: string | null;
  }>;
}

// 직원 정보 수정 이력 목록 조회
export interface HistoryListQuery {
  employeeNumber?: string;
  type?: string;
  memo?: string;
  ipAddress?: string;
  atFrom?: string;
  atTo?: string;
  idAfter?: number;
  cursor?: string;
  size?: number;
  sortField?: "ipAddress" | "at";
  sortDirection?: "asc" | "desc";
}

// 직원 정보 수정 이력 상세 조회
export interface HistoryDetailRequest {
  id: number;
}

// 수정 이력 건수 조회
export interface HistoryCountRequest {
  fromDate?: string;
  toDate?: string;
}
