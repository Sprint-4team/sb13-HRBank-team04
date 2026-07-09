/**
 * 백업 상태
 * - IN_PROGRESS: 진행중
 * - COMPLETED: 완료
 * - SKIPPED: 건너뜀
 * - FAILED: 실패
 */
export type BackupStatus = "IN_PROGRESS" | "COMPLETED" | "SKIPPED" | "FAILED";
export type BackupStatusFilter = BackupStatus | "ALL";

/**
 * 백업 정렬 필드
 */
export type BackupSortField = "startedAt" | "endedAt" | "status";

/**
 * 정렬 방향
 */
export type BackupSortDirection = "ASC" | "DESC";

/**
 * 데이터 백업 이력 DTO
 */
export interface BackupDto {
  id: number;
  worker: string;
  startedAt: string;
  endedAt: string;
  status: BackupStatus;
  fileId: number;
}

/**
 * 백업 목록 조회 쿼리
 */
export interface BackupListQuery {
  worker?: string;
  status?: BackupStatus; // 요청 쿼리는 IN_PROGRESS, COMPLETED, FAILED
  startedAtFrom?: string;
  startedAtTo?: string;
  idAfter?: number;
  cursor?: string;
  size?: number; // 기본 10
  sortField?: "startedAt" | "endedAt" | "status";
  sortDirection?: "ASC" | "DESC";
}

/**
 * 최근 백업 조회 쿼리
 */
export type LatestBackupStatus = "COMPLETED" | "FAILED" | "IN_PROGRESS" | "SKIPPED";

export interface LatestBackupQuery {
  status?: BackupStatus;
}
