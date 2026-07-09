import { getDepartments } from "@/api/department/departmentApi";
import type { DepartmentDto } from "@/model/department";
import type { CursorPageResponse } from "@/model/pagination";
import { create } from "zustand";

interface DepartmentFilterState {
  nameOrDescription: string;
  sortField: "name" | "establishedDate";
  sortDirection: "asc" | "desc";
}

interface DepartmentListState {
  items: DepartmentDto[];
  isLoading: boolean;
  errorMessage?: string;
  hasNext: boolean;
  idAfter: number;
  nextCursor: string | null;
  totalElements: number;
  filters: DepartmentFilterState;
  setFilters: (partial: Partial<DepartmentFilterState>) => void;
  resetFilters: () => void;
  loadFirstPage: () => Promise<void>;
  loadNextPage: () => Promise<void>;
}

const initialFilters: DepartmentFilterState = {
  nameOrDescription: "",
  sortField: "establishedDate",
  sortDirection: "desc",
};

export const useDepartmentListStore = create<DepartmentListState>((set, get) => ({
  items: [],
  isLoading: false,
  errorMessage: undefined,
  hasNext: false,
  idAfter: 0,
  nextCursor: null,
  totalElements: 0,
  filters: initialFilters,

  setFilters: (partial) =>
    set((state) => ({
      filters: { ...state.filters, ...partial },
    })),

  resetFilters: () =>
    set({
      filters: initialFilters,
    }),

  loadFirstPage: async () => {
    const { filters, isLoading } = get();

    if (isLoading) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const page: CursorPageResponse<DepartmentDto> = await getDepartments({
        nameOrDescription: filters.nameOrDescription || undefined,
        sortField: filters.sortField,
        sortDirection: filters.sortDirection,
      });

      set({
        items: page.content,
        hasNext: page.hasNext,
        idAfter: page.nextIdAfter,
        nextCursor: page.nextCursor ?? null,
        isLoading: false,
        totalElements: page.totalElements,
      });
    } catch (error) {
      const message = "부서 불러오기 중 오류";
      console.log(error);

      set({
        isLoading: false,
        errorMessage: message,
      });
    }
  },
  loadNextPage: async () => {
    const { filters, hasNext, nextCursor, items, idAfter, isLoading } = get();

    if (isLoading) return;
    if (!hasNext || !nextCursor) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const page: CursorPageResponse<DepartmentDto> = await getDepartments({
        nameOrDescription: filters.nameOrDescription || undefined,
        sortField: filters.sortField,
        sortDirection: filters.sortDirection,
        idAfter: idAfter,
        cursor: nextCursor,
      });

      set({
        items: [...items, ...page.content],
        hasNext: page.hasNext,
        idAfter: page.nextIdAfter,
        nextCursor: page.nextCursor ?? null,
        isLoading: false,
      });
    } catch (error) {
      const message = "부서 불러오기 중 오류";
      console.log(error);

      set({
        isLoading: false,
        errorMessage: message,
      });
    }
  },
}));
