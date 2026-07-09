import { create } from "zustand";
import { getEmployeeDistribution } from "@/api/employee/employeeApi";
import type {
  EmployeeDistributionDto,
  EmployeeDistributionQuery,
} from "@/model/employee";

interface EmployeeDistributionStore {
  departmentDistributions: EmployeeDistributionDto[];
  positionDistributions: EmployeeDistributionDto[];
  isLoading: boolean;
  getDistributions: () => Promise<void>;
}

const initialFilters: EmployeeDistributionQuery = {
  status: "ACTIVE", // 기본: 재직중
};

export const useEmployeeDistributionStore = create<EmployeeDistributionStore>(
  (set, get) => ({
    departmentDistributions: [],
    positionDistributions: [],
    isLoading: false,
    getDistributions: async () => {
      const { isLoading } = get();

      if (isLoading) return;

      set({ isLoading: true });

      try {
        const [departmentData, positionData] = await Promise.all([
          getEmployeeDistribution({
            groupBy: "department",
            ...initialFilters,
          }),
          getEmployeeDistribution({
            groupBy: "position",
            ...initialFilters,
          }),
        ]);

        set({
          departmentDistributions: departmentData,
          positionDistributions: positionData,
          isLoading: false,
        });
      } catch (error) {
        console.error(error);
        set({
          isLoading: false,
        });
      }
    },
  }),
);
