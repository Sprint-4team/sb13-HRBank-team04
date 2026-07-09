import type {
  HistoryCountRequest,
  HistoryDetailRequest,
  HistoryDto,
  HistoryListQuery,
} from "@/model/history";
import type { CursorPageResponse } from "@/model/pagination";
import apiClient from "../client";

export interface HistoryDetailDto extends HistoryDto {
  propertyName: string;
  before: unknown;
  after: unknown;
}

export interface HistoryCountDto {
  count: number;
}

// 직원 정보 수정 이력 목록을 조회합니다. (페이징, 필터링 가능)
export function getChangeLogs(
  query: HistoryListQuery,
): Promise<CursorPageResponse<HistoryDto>> {
  return apiClient.get<CursorPageResponse<HistoryDto>>("/change-logs", query);
}

//  직원 정보 수정 이력의 상세 정보를 조회합니다.
export async function getChangeLogDetails({
  id,
}: HistoryDetailRequest): Promise<HistoryDetailDto> {
  // Path Parameter는 URL에 직접 삽입됩니다.
  const response = await apiClient.get<HistoryDetailDto>(`/change-logs/${id}`);

  return response;
}

// 직원 정보 수정 이력 건수
export async function getChangeLogsCount(
  query?: HistoryCountRequest,
): Promise<HistoryCountDto> {
  // 쿼리 파라미터가 있다면 전송하고, 없다면 빈 객체를 전송합니다.
  const params = query
    ? Object.fromEntries(
        Object.entries(query).filter(
          ([_, value]) => value !== undefined && value !== null && value !== "",
        ),
      )
    : {};

  const response = await apiClient.get<HistoryCountDto>("/change-logs/count", {
    params: params,
  });

  return response;
}

export async function getRecentChangeCount(): Promise<number> {
  return await apiClient.get(`/change-logs/count`);
}
