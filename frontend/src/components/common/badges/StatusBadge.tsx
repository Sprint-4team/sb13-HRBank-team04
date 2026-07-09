import type {
  BadgeColors,
  BadgeTypes,
} from "@/components/common/badges/badge-types";
import { BackupStateLabels } from "@/constants/BackupStateLabels";
import { EmploymentStateLabels } from "@/constants/EmploymentStateLabels";
import { HistoryTypeLabels } from "@/constants/HistoryTypeLabels";
import {
  BackupState,
  EmploymentState,
  HistoryType,
  type BackupStateType,
  type EmploymentStateType,
} from "@/types/enums";
import { Badge, BadgeWithDot } from "./Badges";

export type StatusKind = "employment" | "backup" | "history";
export type StatusValue = EmploymentStateType | BackupStateType | HistoryType;

const employmentConfig: Record<string, StatusConfig> = {
  [EmploymentState.ACTIVE]: {
    label: EmploymentStateLabels[EmploymentState.ACTIVE],
    type: "pill-color",
    color: "purple",
  },
  [EmploymentState.ON_LEAVE]: {
    label: EmploymentStateLabels[EmploymentState.ON_LEAVE],
    type: "pill-color",
    color: "orange",
  },
  [EmploymentState.RESIGNED]: {
    label: EmploymentStateLabels[EmploymentState.RESIGNED],
    type: "pill-color",
    color: "gray",
  },
};

const backupConfig: Record<string, StatusConfig> = {
  [BackupState.COMPLETED]: {
    label: BackupStateLabels[BackupState.COMPLETED],
    type: "color",
    color: "success",
  },
  [BackupState.IN_PROGRESS]: {
    label: BackupStateLabels[BackupState.IN_PROGRESS],
    type: "color",
    color: "blue",
  },
  [BackupState.FAILED]: {
    label: BackupStateLabels[BackupState.FAILED],
    type: "color",
    color: "warning",
  },
  [BackupState.SKIPPED]: {
    label: BackupStateLabels[BackupState.SKIPPED],
    type: "color",
    color: "gray-blue",
  },
};

const historyConfig: Record<
  HistoryType,
  { label: string; type: BadgeTypes; color: BadgeColors; withDot: boolean }
> = {
  [HistoryType.ALL]: {
    label: HistoryTypeLabels[HistoryType.ALL],
    type: "pill-color",
    color: "success",
    withDot: true,
  },
  [HistoryType.CREATED]: {
    label: HistoryTypeLabels[HistoryType.CREATED],
    type: "pill-color",
    color: "success",
    withDot: true,
  },
  [HistoryType.UPDATED]: {
    label: HistoryTypeLabels[HistoryType.UPDATED],
    type: "pill-color",
    color: "blue-light",
    withDot: true,
  },
  [HistoryType.DELETED]: {
    label: HistoryTypeLabels[HistoryType.DELETED],
    type: "pill-color",
    color: "orange",
    withDot: true,
  },
};

type StatusConfig = {
  label: string;
  type: BadgeTypes;
  color: BadgeColors;
  withDot?: boolean;
};

interface StatusBadgeProps {
  kind: StatusKind;
  value: string;
  className?: string;
}

export const StatusBadge = ({ kind, value, className }: StatusBadgeProps) => {
  let config: StatusConfig | undefined;

  if (kind === "employment") {
    config = employmentConfig[value as EmploymentStateType];
  } else if (kind === "backup") {
    config = backupConfig[value as BackupStateType];
  } else if (kind === "history") {
    config = historyConfig[value as HistoryType];
  }

  if (!config) return null;

  const { type, color, label, withDot } = config;

  return withDot ? (
    <BadgeWithDot type={type} color={color} className={className}>
      {label}
    </BadgeWithDot>
  ) : (
    <Badge type={type} color={color} className={className}>
      {label}
    </Badge>
  );
};
