import type {
  BackupDto,
  BackupListQuery,
  LatestBackupQuery,
} from "@/model/backup";
import type { CursorPageResponse } from "@/model/pagination";
import apiClient from "../client";

/**
 * 데이터 백업 목록 조회
 */
export function getBackups(
  query: BackupListQuery,
): Promise<CursorPageResponse<BackupDto>> {
  return apiClient.get<CursorPageResponse<BackupDto>>("/backups", query);
}

/**
 * 데이터 백업 생성
 */
export function createBackup(): Promise<BackupDto> {
  return apiClient.post<BackupDto>("/backups");
}

/**
 * 가장 최근 백업 정보 조회
 */
export function getLatestBackup(query?: LatestBackupQuery): Promise<BackupDto> {
  return apiClient.get<BackupDto>("/backups/latest", query);
}
