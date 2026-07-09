"use client";

import type { TooltipProps } from "recharts";
import type { Props as LegendContentProps } from "recharts/types/component/DefaultLegendContent";
import type {
  NameType,
  ValueType,
} from "recharts/types/component/DefaultTooltipContent";
import { cx } from "@/utils/cx";

/**
 * Renders the legend content for a chart.
 * @param payload - The payload of the legend.
 * @param align - The alignment of the legend.
 * @param layout - The layout of the legend.
 * @param className - The class name of the legend.
 * @returns The legend content.
 */
export const ChartLegendContent = ({
  payload,
  align,
  layout,
  className,
}: LegendContentProps & { reversed?: boolean; className?: string }) => {
  return (
    <ul
      className={cx(
        "flex",
        layout === "vertical"
          ? `flex-col gap-1 pl-4 ${
              align === "center"
                ? "items-center"
                : align === "right"
                  ? "items-start"
                  : "items-start"
            }`
          : `flex-row gap-3 ${
              align === "center"
                ? "justify-center"
                : align === "right"
                  ? "justify-end"
                  : "justify-start"
            }`,
        className,
      )}
    >
      {payload?.map((entry, index) => (
        <li
          className="text-tertiary flex items-center gap-2 text-sm"
          key={index}
        >
          <span
            className={cx(
              "h-2 w-2 rounded-full bg-current",
              (entry.payload as { className?: string })?.className,
            )}
          />
          {entry.value}
        </li>
      ))}
    </ul>
  );
};

interface ChartTooltipContentProps extends TooltipProps<ValueType, NameType> {
  isRadialChart?: boolean;
  isPieChart?: boolean;
  label?: string;
  // We have to use `any` here because the `payload` prop is not typed correctly in the `recharts` library.
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  payload?: any;
}

export const ChartTooltipContent = ({
  active,
  payload,
  labelFormatter,
}: ChartTooltipContentProps) => {
  const canRender = active && payload && payload.length;

  if (!canRender) {
    return null;
  }

  const isIncreasing = payload[0].payload.change >= 0;
  const isSame = payload[0].payload.change === 0;

  const change = payload[0].payload.change;
  const changeRate = payload[0].payload.changeRate;
  const count = Number(payload[0].payload.count).toLocaleString();
  const date = payload[0].payload.date;
  const formattedDate = labelFormatter && labelFormatter(date, payload);

  return (
    <div className="flex w-fit min-w-32 flex-col gap-3 rounded-lg bg-white px-4 py-3 shadow-lg">
      <p className="text-sm font-semibold">{formattedDate}</p>
      <hr className="border-tertiary" />
      <div className="text-tertiary grid grid-cols-2 gap-y-2 text-xs font-normal">
        <p className="text-start">직원 수</p>
        <p className="text-secondary text-end font-medium">{count}명</p>
        <p className="text-start">증감</p>
        <p
          className={cx(
            "text-end font-semibold",
            isSame
              ? "text-secondary"
              : isIncreasing
                ? "text-green-600"
                : "text-red-500",
          )}
        >
          {isIncreasing && !isSame && "+"}
          {change}명
        </p>
        <p className="text-start">증감률</p>
        <p
          className={cx(
            "text-end font-semibold",
            isSame
              ? "text-secondary"
              : isIncreasing
                ? "text-green-600"
                : "text-red-500",
          )}
        >
          {isIncreasing && !isSame && "+"}
          {changeRate}명
        </p>
      </div>
    </div>
  );
};
