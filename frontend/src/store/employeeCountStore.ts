import { create } from "zustand";
import { getEmployeeCount } from "@/api/employee/employeeApi";
import { formatDateThisMonth } from "@/utils/date";

interface EmployeeCountStore {
  count: number | null;
  monthCount: number | null;
  isLoading: boolean;
  isLoadingMonthCount: boolean;
  errorMessage: string | null;
  getCount: () => Promise<void>;
  getThisMonthCount: () => Promise<void>;
}

export const useEmployeeCountStore = create<EmployeeCountStore>(
  (set, get) => ({
    count: null,
    monthCount: null,
    isLoading: false,
    isLoadingMonthCount: false,
    errorMessage: null,

    getCount: async () => {
      const { isLoading } = get();

      if (isLoading) return;

      set({ isLoading: true, errorMessage: null });

      try {
        const activeData = await getEmployeeCount({ status: "ACTIVE" });
        const onLeaveData = await getEmployeeCount({ status: "ON_LEAVE" });
        const total = activeData + onLeaveData;
        set({ count: total, isLoading: false });
      } catch (error) {
        console.error(error);
        set({
          errorMessage: "직원 수를 불러오지 못했어요.",
          isLoading: false,
        });
      }
    },

    getThisMonthCount: async () => {
      const { isLoadingMonthCount } = get();

      if (isLoadingMonthCount) return;

      set({ isLoadingMonthCount: true, errorMessage: null });

      const date = formatDateThisMonth(new Date());

      try {
        const data = await getEmployeeCount({
          status: "ACTIVE",
          fromDate: date,
        });
        set({ monthCount: data, isLoadingMonthCount: false });
      } catch (error) {
        console.error(error);
        set({
          errorMessage: "직원 수를 불러오지 못했어요.",
          isLoadingMonthCount: false,
        });
      }
    },
  }),
);
