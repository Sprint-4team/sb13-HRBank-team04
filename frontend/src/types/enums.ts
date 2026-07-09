export const EmploymentState = {
  ALL: "ALL",
  ACTIVE: "ACTIVE",
  ON_LEAVE: "ON_LEAVE",
  RESIGNED: "RESIGNED",
} as const;

export type EmploymentStateType =
  (typeof EmploymentState)[keyof typeof EmploymentState];

export const EmploymentEnableState = {
  ACTIVE: "ACTIVE",
  ON_LEAVE: "ON_LEAVE",
  RESIGNED: "RESIGNED",
} as const;

export type EmploymentEnableStateType =
  (typeof EmploymentEnableState)[keyof typeof EmploymentEnableState];

export const BackupState = {
  IN_PROGRESS: "IN_PROGRESS",
  COMPLETED: "COMPLETED",
  FAILED: "FAILED",
  SKIPPED: "SKIPPED",
} as const;

export type BackupStateType = (typeof BackupState)[keyof typeof BackupState];

export const HistoryType = {
  ALL: "ALL",
  CREATED: "CREATED",
  UPDATED: "UPDATED",
  DELETED: "DELETED",
} as const;

export type HistoryType = (typeof HistoryType)[keyof typeof HistoryType];
export type EmployeeStatus = "ACTIVE" | "ON_LEAVE" | "RESIGNED" | "ALL" | "";

// 분포
export type EmployeeDistributionGroupBy = "department" | "position";
