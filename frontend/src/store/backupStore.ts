import { getBackups, getLatestBackup } from "@/api/backup/backupApi";
import type { BackupDto, BackupListQuery } from "@/model/backup";
import type { CursorPageResponse } from "@/model/pagination";
import { create } from "zustand";

interface BackupListState {
  items: BackupDto[];
  isLoading: boolean;
  errorMessage?: string;
  hasNext: boolean;
  idAfter: number;
  nextCursor: string | null;
  totalElements: number;
  latestBackup?: BackupDto | null;
  filters: BackupListQuery;
  setFilters: (partial: Partial<BackupListQuery>) => void;
  resetFilters: () => void;
  loadFirstPage: () => Promise<void>;
  loadNextPage: () => Promise<void>;
  getLatestBackup: () => Promise<void>;
}

const initialFilters: BackupListQuery = {
  sortField: "startedAt",
  sortDirection: "DESC",
};

export const useBackupListStore = create<BackupListState>((set, get) => ({
  items: [],
  totalElements: 0,
  isLoading: false,
  errorMessage: undefined,
  hasNext: false,
  idAfter: 0,
  nextCursor: null,
  latestBackup: null,
  filters: initialFilters,

  setFilters: (filters) => {
    set((state) => ({
      filters: {
        ...state.filters,
        ...filters,
      },
    }));
  },

  resetFilters: () => {
    set({
      filters: initialFilters,
    });
  },

  loadFirstPage: async () => {
    const { filters, isLoading } = get();

    if (isLoading) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const res: CursorPageResponse<BackupDto> = await getBackups({
        ...filters,
      });

      set({
        items: res.content,
        totalElements: res.totalElements,
        hasNext: res.hasNext,
        idAfter: res.nextIdAfter,
        nextCursor: res.nextCursor ?? null,
        isLoading: false,
      });
    } catch (error) {
      console.error(error);
      set({
        isLoading: false,
        errorMessage: "백업 목록을 불러오는 중 오류가 발생했어요.",
      });
    }
  },

  loadNextPage: async () => {
    const { hasNext, idAfter, nextCursor, filters, items, isLoading } = get();

    if (isLoading) return;
    if (!hasNext || !nextCursor) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const res: CursorPageResponse<BackupDto> = await getBackups({
        ...filters,
        idAfter: idAfter,
        cursor: nextCursor,
      });

      set({
        items: [...items, ...res.content],
        totalElements: res.totalElements,
        hasNext: res.hasNext,
        idAfter: res.nextIdAfter,
        nextCursor: res.nextCursor ?? null,
        isLoading: false,
      });
    } catch (error) {
      console.error(error);
      set({
        isLoading: false,
        errorMessage: "백업 목록을 불러오는 중 오류가 발생했어요.",
      });
    }
  },
  // 최신 백업 데이터 조회
  getLatestBackup: async () => {
    const { items, isLoading } = get();

    if (isLoading) return;
    if (!items) return;

    set({ isLoading: true, errorMessage: undefined });

    try {
      const data: BackupDto = await getLatestBackup();
      set({
        latestBackup: data,
        isLoading: false,
      });
    } catch (error) {
      console.log(error);
      set({
        isLoading: false,
        errorMessage: "마지막 백업을 불러오는 중 오류가 발생했어요.",
      });
    }
  },
}));
