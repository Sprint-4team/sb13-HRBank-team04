import { HistoryType } from "@/types/enums";

export const HistoryTypeLabels: Record<HistoryType, string> = {
  [HistoryType.ALL]: "전체",
  [HistoryType.CREATED]: "직원 추가",
  [HistoryType.UPDATED]: "정보 수정",
  [HistoryType.DELETED]: "직원 삭제",
};
