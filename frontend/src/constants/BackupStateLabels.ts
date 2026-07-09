import { BackupState, type BackupStateType } from "@/types/enums";

export const BackupStateLabels: Record<BackupStateType, string> = {
  [BackupState.IN_PROGRESS]: "진행중",
  [BackupState.COMPLETED]: "완료",
  [BackupState.FAILED]: "실패",
  [BackupState.SKIPPED]: "건너뜀",
};

export const BackupStatusFilterLabels = {
  ALL: "전체",
  ...BackupStateLabels,
};
