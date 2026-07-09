import { useEffect, useMemo } from "react";
import type { FC } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Briefcase01, Building05 } from "@untitledui/icons";
import type { EmployeeDistributionDto } from "@/model/employee";
import { useEmployeeDistributionStore } from "@/store/employeeDistributionStore";

type DistributionType = "department" | "position";

interface EmployeeDistributionCardProps {
  type: DistributionType;
}

const SEGMENT_COLORS = [
  "bg-indigo-500",
  "bg-emerald-400",
  "bg-sky-400",
  "bg-amber-300",
  "bg-rose-300",
  "bg-gray-200",
];

export function EmployeeDistributionCard({
  type,
}: EmployeeDistributionCardProps) {
  const {
    departmentDistributions,
    positionDistributions,
    isLoading,
    getDistributions,
  } = useEmployeeDistributionStore();

  // 마운트 시 한 번만 조회
  useEffect(() => {
    getDistributions();
  }, [getDistributions]);

  // 타입에 따라 사용할 원본 데이터 선택
  const rawItems: EmployeeDistributionDto[] =
    type === "department" ? departmentDistributions : positionDistributions;

  // 비율 + 색상 계산
  const segments = useMemo(() => {
    if (!rawItems || rawItems.length === 0) return [];

    const total = rawItems.reduce((sum, item) => sum + item.count, 0) || 1;

    return rawItems.map((item, index) => ({
      ...item,
      ratio: item.count / total,
      colorClass: SEGMENT_COLORS[index % SEGMENT_COLORS.length],
    }));
  }, [rawItems]);

  const title = type === "department" ? "부서별 직원 분포" : "직무별 직원 분포";
  const Icon: FC<{ className?: string }> =
    type === "department" ? Building05 : Briefcase01;

  return (
    <section className="flex h-full flex-col rounded-2xl border border-gray-100 bg-gray-50 p-4">
      {/* 헤더 */}
      <div className="mb-4 flex items-center gap-2">
        <div className="flex h-10 w-10 items-center justify-center rounded-lg border border-gray-200 bg-white">
          <Icon className="h-5 w-5 text-gray-700" aria-hidden="true" />
        </div>
        <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
      </div>

      <div className="rounded-lg bg-white px-5 py-6">
        {/* 상단 분포 바 (가로 막대) */}
        {segments.length !== 0 && (
          <div className="mb-4 flex h-10 w-full gap-1 rounded-xl p-1">
            {segments.map((seg) => (
              <div
                key={seg.groupKey}
                className={`h-full rounded-lg ${seg.colorClass}`}
                style={{ flex: Math.max(seg.ratio, 0.06) }} // 최소 너비 확보
                title={`${seg.groupKey} ${seg.percentage?.toFixed?.(1) ?? ""}%`}
              />
            ))}
          </div>
        )}

        {/* 하단 리스트 (스크롤 가능) */}
        <div className="scrollbar-thin h-44 flex-1 space-y-1 overflow-y-auto pr-1">
          {segments.map(
            (
              seg: EmployeeDistributionDto & {
                ratio: number;
                colorClass: string;
              },
            ) => (
              <div
                key={seg.groupKey}
                className="flex items-center justify-between gap-3 py-1 text-xs sm:text-sm"
              >
                <div className="flex min-w-0 items-center gap-2">
                  <span
                    className={`h-6 w-6 rounded-lg ${seg.colorClass}`}
                    aria-hidden="true"
                  />
                  <span className="text-tertiary text-md truncate font-semibold">
                    {seg.groupKey || "-"}
                  </span>
                </div>
                <span className="text-secondary text-md font-semibold whitespace-nowrap">
                  {seg.count.toLocaleString()}명
                </span>
              </div>
            ),
          )}

          {!isLoading && segments.length === 0 && (
            <div className="flex h-full flex-col items-center justify-center gap-3 p-3">
              <p className="text-center text-sm text-gray-500">
                {`아직 등록된 ${type === "department" ? "부서가" : "직원이"} 없어요`}
              </p>
              <Link
                to={`/${type === "department" ? "departments" : "employees"}`}
                className="text-secondary border-primary flex items-center gap-1 rounded-lg border px-7 py-2.5 text-sm font-semibold"
              >
                {type === "department"
                  ? "부서 등록하러 가기"
                  : "직원 등록하러 가기"}
                <ArrowRight size={20} />
              </Link>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
