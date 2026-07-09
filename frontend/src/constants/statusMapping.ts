import { EmploymentState, BackupState, HistoryType } from "@/types/enums";

export const statusMapping = {
  employment: {
    재직중: EmploymentState.ACTIVE,
    휴직중: EmploymentState.ON_LEAVE,
    퇴사: EmploymentState.RESIGNED,
    전체: EmploymentState.ALL,
  },
  backup: {
    완료: BackupState.COMPLETED,
    진행중: BackupState.IN_PROGRESS,
    실패: BackupState.FAILED,
  },
  history: {
    CREATED: HistoryType.CREATED,
    UPDATED: HistoryType.UPDATED,
    DELETED: HistoryType.DELETED,
  },
} as const;
