import { create } from "zustand";
import { getEmployeeTrend } from "@/api/employee/employeeApi";
import type { EmployeeTrendDto, EmployeeTrendQuery } from "@/model/employee";

interface EmployeeTrendStore {
  items: EmployeeTrendDto[];
  filters: EmployeeTrendQuery;
  isLoading: boolean;
  errorMessage: string | null;
  setFilters: (filters: Partial<EmployeeTrendQuery>) => void;
  resetFilters: () => void;
  getTrend: () => Promise<void>;
}

const initialFilters: EmployeeTrendQuery = {
  unit: "month", // 기본: 월별 추이
};

export const useEmployeeTrendStore = create<EmployeeTrendStore>((set, get) => ({
  items: [],
  filters: initialFilters,
  isLoading: false,
  errorMessage: null,

  setFilters: (partial) =>
    set((state) => ({
      filters: { ...state.filters, ...partial },
    })),

  resetFilters: () =>
    set({
      filters: initialFilters,
    }),

  getTrend: async () => {
    const { filters, isLoading } = get();

    if (isLoading) return;

    set({ isLoading: true, errorMessage: null });

    try {
      const data = await getEmployeeTrend(filters);
      set({ items: data, isLoading: false });
    } catch (error) {
      console.error(error);
      set({
        errorMessage: "직원 수 추이를 불러오지 못했어요.",
        isLoading: false,
      });
    }
  },
}));
