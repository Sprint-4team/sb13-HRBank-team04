import type { FC } from "react";

interface StatCardProps {
  icon: FC<{ className?: string }>;
  label: string;
  value: number | null;
  unit: string;
}

export function StatCard({ icon: Icon, label, value, unit }: StatCardProps) {
  const display =
    value === null || value === undefined ? "-" : value.toLocaleString();

  return (
    <div className="border-secondary flex min-w-0 gap-4 rounded-xl border bg-white p-5 shadow-xs">
      <Icon className="border-secondary size-10 shrink-0 rounded-md border p-2.5 shadow-xs" />
      <div className="min-w-0 flex-1">
        <div className="text-md text-tertiary leading-6 font-medium">
          {label}
        </div>
        <p className="text-display-sm truncate font-semibold text-gray-900">
          {display}
          {display !== "-" ? unit : null}
        </p>
      </div>
    </div>
  );
}
