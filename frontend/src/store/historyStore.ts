import { create } from "zustand";
import { getChangeLogs, getRecentChangeCount } from "@/api/history/historyApi";
import type { HistoryDto, HistoryListQuery } from "@/model/history";
import type { CursorPageResponse } from "@/model/pagination";

interface HistoryFilterState {
  employeeNumber?: string;
  type?: string;
  memo?: string;
  ipAddress?: string;
  atFrom?: string; // $date-time
  atTo?: string; // $date-time
  sortField: "ipAddress" | "at";
  sortDirection: "asc" | "desc";
  size: number;
}

interface HistoryListState {
  items: HistoryDto[];
  isLoading: boolean;
  // 필터링(loadFirstPage) 중인지 나타내는 상태
  isFiltering: boolean;
  errorMessage?: string;
  idAfter: number;
  hasNext: boolean;
  nextCursor: string | null;
  totalElements: number;
  // nextIdAfter: number | null;
  recentChangeCount: number | null;
  filters: HistoryFilterState;

  setFilters: (partial: Partial<HistoryFilterState>) => void;
  resetFilters: () => void;

  loadFirstPage: () => Promise<void>;
  loadNextPage: () => Promise<void>;
  getRecentChangeCount: () => Promise<void>;
}

const initialFilters: HistoryFilterState = {
  employeeNumber: "",
  type: "",
  memo: "",
  ipAddress: "",
  atFrom: "",
  atTo: "",
  sortField: "at",
  sortDirection: "desc",
  size: 20,
};

export const useHistoryListStore = create<HistoryListState>((set, get) => ({
  items: [],
  isLoading: false,
  isFiltering: false,
  errorMessage: undefined,
  hasNext: false,
  idAfter: 0,
  nextCursor: null,
  recentChangeCount: null,
  filters: initialFilters,
  totalElements: 0,
  // nextIdAfter: null,

  // 필터 업데이트 액션
  setFilters: (partial) =>
    set((state) => ({
      filters: { ...state.filters, ...partial },
    })),

  // 필터 초기화 액션
  resetFilters: () =>
    set({
      filters: initialFilters,
    }),

  // 첫 페이지 로딩 (커서와 idAfter 없이)
  loadFirstPage: async () => {
    const { filters, isLoading, isFiltering } = get();

    if (isLoading || isFiltering) return;

    set({
      isLoading: true,
      isFiltering: true,
      errorMessage: undefined,
      items: [],
      hasNext: false,
      nextCursor: null,
    });

    try {
      const page: CursorPageResponse<HistoryDto> = await getChangeLogs({
        employeeNumber: filters.employeeNumber,
        type: filters.type,
        memo: filters.memo,
        ipAddress: filters.ipAddress,
        atFrom: filters.atFrom,
        atTo: filters.atTo,
        sortField: filters.sortField,
        sortDirection: filters.sortDirection,
        size: filters.size,
      });

      set({
        items: page.content,
        hasNext: page.hasNext,
        nextCursor: page.nextCursor ?? null,
        totalElements: page.totalElements ?? 0,
        // nextIdAfter: page.nextIdAfter ?? null,
        isLoading: false,
        isFiltering: false,
      });
    } catch (error) {
      const message = "변경 이력 불러오기 중 오류";
      console.error(error);

      set({
        isLoading: false,
        isFiltering: false,
        errorMessage: message,
      });
    }
  },

  // 다음 페이지 로딩 (현재 커서 정보 사용)
  loadNextPage: async () => {
    const {
      filters,
      idAfter,
      hasNext,
      nextCursor,
      items,
      isLoading,
      isFiltering,
    } = get();
    // 이미 로딩 중이거나 필터중이라면 바로 종료 (동시 호출 방지)
    if (isLoading || isFiltering) return;
    // 다음 페이지가 없거나 커서가 없으면 종료
    if (!hasNext || !nextCursor) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const query: HistoryListQuery = {
        ...filters,
        // 다음 페이지 로딩 시 현재 커서 값을 쿼리에 추가
        cursor: nextCursor,
        idAfter: idAfter,
      };
      const page: CursorPageResponse<HistoryDto> = await getChangeLogs(query);
      // 유효성 검사 (null/undefined 제거)
      const validNewContent = page.content.filter(
        (item) => !!item && !!item.id,
      );

      // 키 중복 제거
      const existingIds = new Set(items.map((item) => item.id));
      const deduplicatedNewContent = validNewContent.filter(
        (item) => !existingIds.has(item.id),
      );
      set((state) => ({
        items: [...state.items, ...deduplicatedNewContent], // 기존 데이터에 새 데이터 추가
        hasNext: page.hasNext,
        nextCursor: page.nextCursor ?? null,
        totalElements: page.totalElements ?? 0,
        idAfter: page.nextIdAfter,
        isLoading: false,
      }));
    } catch (error) {
      const message = "다음 이력 페이지 불러오기 중 오류";
      console.error(error);

      set({
        isLoading: false,
        errorMessage: message,
        nextCursor: null,
        idAfter: 0,
        hasNext: false,
      });
    }
  },

  // 최근 수정 이력 건수 조회
  getRecentChangeCount: async () => {
    try {
      const res = await getRecentChangeCount();
      set({
        recentChangeCount: res,
      });
    } catch (error) {
      console.error("Failed to fetch recent change count:", error);
    }
  },
}));
