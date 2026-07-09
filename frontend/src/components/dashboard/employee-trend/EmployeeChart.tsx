import { useEffect } from "react";
import { Users01 } from "@untitledui/icons";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { ChartTooltipContent } from "@/components/common/charts/Chart";
import { DropdownButton } from "@/components/common/dropdown/DropdownButton";
import { EmployeeTrendFilterLabels } from "@/constants/EmploymentStateLabels";
import type { EmployeeTrendUnit } from "@/model/employee";
import { useEmployeeTrendStore } from "@/store/employeeTrendStore";
import { formatTooltipLabel, formatXAxisTick } from "@/utils/chart";

export function EmployeeChart() {
  const { items, filters, setFilters, getTrend } = useEmployeeTrendStore();

  useEffect(() => {
    getTrend();
  }, [getTrend, filters]);

  return (
    <div className="w-full space-y-2 bg-gray-50 p-2">
      <div className="flex items-center gap-3 px-3">
        <Users01 className="border-secondary size-10 shrink-0 rounded-md border bg-white p-2.5 shadow-xs" />
        <h2 className="flex-1 text-lg font-semibold text-gray-950">
          직원수 변동 추이
        </h2>
        <DropdownButton
          value={filters.unit}
          label={EmployeeTrendFilterLabels}
          placeholder="월별"
          onChange={(value) => setFilters({ unit: value as EmployeeTrendUnit })}
        />
      </div>
      <div className="custom-chart-focus-fix h-[300px] rounded-lg bg-white pr-5">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart
            data={items}
            className="text-tertiary outline-none [&_.recharts-text]:text-xs"
            margin={{
              top: 25,
              bottom: 16,
            }}
          >
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="0" y2="1">
                <stop
                  offset="5%"
                  stopColor="currentColor"
                  className="text-utility-brand-700"
                  stopOpacity="0.7"
                />
                <stop
                  offset="95%"
                  stopColor="currentColor"
                  className="text-utility-brand-700"
                  stopOpacity="0"
                />
              </linearGradient>
            </defs>

            <CartesianGrid
              vertical={false}
              stroke="currentColor"
              className="text-utility-gray-100"
            />

            <XAxis
              fill="currentColor"
              axisLine={false}
              tickLine={false}
              interval="preserveStartEnd"
              dataKey="date"
              tickFormatter={(value: string, index: number) =>
                formatXAxisTick(value, index, filters.unit ?? "month")
              }
              padding={{ left: 16, right: 10 }}
            />

            <YAxis
              fill="currentColor"
              axisLine={false}
              tickLine={false}
              interval="preserveStartEnd"
              tickFormatter={(value) => `${Number(value).toLocaleString()}명`}
            />

            <Tooltip
              content={<ChartTooltipContent />}
              labelFormatter={(value: string) =>
                formatTooltipLabel(value, filters.unit ?? "month")
              }
              cursor={{
                className: "stroke-utility-brand-600 stroke-0",
              }}
            />

            <Area
              isAnimationActive={true}
              className="text-utility-brand-600 [&_.recharts-area-area]:translate-y-1.5 [&_.recharts-area-area]:[clip-path:inset(0_0_6px_0)]"
              dataKey="count"
              name="Employee Count"
              type="monotone"
              stroke="currentColor"
              strokeWidth={2}
              fill="url(#gradient)"
              fillOpacity={0.1}
              activeDot={{
                r: 6,
                className: "fill-bg-primary stroke-utility-brand-600 stroke-2",
              }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
