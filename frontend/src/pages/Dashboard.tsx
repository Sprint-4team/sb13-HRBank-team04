import { EmployeeDistributionCard } from "@/components/dashboard/distribution/DistributionCard";
import { EmployeeChart } from "@/components/dashboard/employee-trend/EmployeeChart";
import StatSection from "@/components/dashboard/stat/StatSection";

export default function Dashboard() {
  return (
    <div className="space-y-3 pb-20">
      {/* 대시보드 지표 */}
      <StatSection />

      <div className="space-y-3 rounded-xl bg-white p-5 shadow-xs">
        {/* 직원수 변동 추이 차트 */}
        <EmployeeChart />
        {/* 직원 분포 */}
        <div className="grid grid-cols-2 gap-3 max-xl:grid-cols-1">
          <EmployeeDistributionCard type="department" />
          <EmployeeDistributionCard type="position" />
        </div>
      </div>
    </div>
  );
}
