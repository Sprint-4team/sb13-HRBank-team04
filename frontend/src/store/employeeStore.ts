import { create } from "zustand";
import { getEmployees } from "@/api/employee/employeeApi";
import type { EmployeeDto } from "@/model/employee";
import type { CursorPageResponse } from "@/model/pagination";
import type { EmployeeStatus } from "@/types/enums";

interface EmployeeFilterState {
  nameOrEmail: string;
  departmentName: string;
  position: string;
  status?: EmployeeStatus;
  employeeNumber?: string;
  hireDateFrom?: string;
  hireDateTo?: string;
  sortField: "name" | "employeeNumber" | "hireDate";
  sortDirection: "asc" | "desc";
  size: number;
}

interface EmployeeListState {
  items: EmployeeDto[];
  isLoading: boolean;
  errorMessage?: string;
  hasNext: boolean;
  nextCursor: string | null;
  totalElements: number;
  filters: EmployeeFilterState;

  setFilters: (partial: Partial<EmployeeFilterState>) => void;
  resetFilters: () => void;

  loadFirstPage: () => Promise<void>;
  loadNextPage: () => Promise<void>;
}

const initialFilters: EmployeeFilterState = {
  nameOrEmail: "",
  departmentName: "",
  position: "",
  sortField: "hireDate",
  sortDirection: "desc",
  size: 10,
};

export const useEmployeeListStore = create<EmployeeListState>((set, get) => ({
  items: [],
  isLoading: false,
  errorMessage: undefined,
  hasNext: false,
  nextCursor: null,
  filters: initialFilters,
  totalElements: 0,

  setFilters: (partial) =>
    set((state) => ({
      filters: { ...state.filters, ...partial },
    })),

  resetFilters: () =>
    set({
      filters: initialFilters,
    }),

  // 첫 페이지 로딩
  loadFirstPage: async () => {
    const { filters, isLoading } = get();

    if (isLoading) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const page: CursorPageResponse<EmployeeDto> = await getEmployees({
        nameOrEmail: filters.nameOrEmail || undefined,
        departmentName: filters.departmentName || undefined,
        position: filters.position || undefined,
        status: filters.status,
        employeeNumber: filters.employeeNumber,
        hireDateFrom: filters.hireDateFrom,
        hireDateTo: filters.hireDateTo,
        size: filters.size,
        sortField: filters.sortField,
        sortDirection: filters.sortDirection,
      });

      set({
        items: page.content,
        hasNext: page.hasNext,
        nextCursor: page.nextCursor ?? null,
        isLoading: false,
        totalElements: page.totalElements ?? 0,
      });
    } catch (error) {
      const message = "직원 불러오기 중 오류";

      set({
        isLoading: false,
        errorMessage: message,
      });
    }
  },

  // 다음 페이지 로딩
  loadNextPage: async () => {
    const { filters, hasNext, nextCursor, items, isLoading } = get();

    if (isLoading) return;
    if (!hasNext || !nextCursor) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const page: CursorPageResponse<EmployeeDto> = await getEmployees({
        nameOrEmail: filters.nameOrEmail || undefined,
        departmentName: filters.departmentName || undefined,
        position: filters.position || undefined,
        status: filters.status,
        employeeNumber: filters.employeeNumber,
        hireDateFrom: filters.hireDateFrom,
        hireDateTo: filters.hireDateTo,
        size: filters.size,
        sortField: filters.sortField,
        sortDirection: filters.sortDirection,
        cursor: nextCursor,
      });

      set({
        items: [...items, ...page.content],
        hasNext: page.hasNext,
        nextCursor: page.nextCursor ?? null,
        totalElements: page.totalElements ?? 0,
        isLoading: false,
      });
    } catch (error) {
      const message = "직원 불러오기 중 오류";

      set({
        isLoading: false,
        errorMessage: message,
      });
    }
  },
}));
