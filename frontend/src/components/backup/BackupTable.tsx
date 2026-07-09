import type { SortDescriptor } from "react-aria-components";
import { Download01 } from "@untitledui/icons";
import { downloadFileById } from "@/api/file/fileApi";
import { StatusBadge } from "@/components/common/badges/StatusBadge";
import { Button } from "@/components/common/buttons/Button";
import { Table } from "@/components/common/table/Table";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import { useBackupListStore } from "@/store/backupStore";
import { useToastStore } from "@/store/toastStore";
import { formatIsoToYmdHms } from "@/utils/date";
import { downloadBlob } from "@/utils/download";
import { isActiveSortColumn } from "@/utils/sort";

export function BackupTable() {
  const { items, isLoading, errorMessage, hasNext, filters, setFilters, loadNextPage } =
    useBackupListStore();
  const { errorToast } = useToastStore();

  // 테이블 정렬 (스토어와 동기화)
  const sortDescriptor: SortDescriptor = {
    column: filters.sortField || "startedAt",
    direction: filters.sortDirection === "DESC" ? "descending" : "ascending",
  };

  // 정렬 변경 핸들러
  const handleSortChange = (descriptor: SortDescriptor) => {
    if (!descriptor.column) return;

    const newSortField = descriptor.column as "startedAt" | "endedAt" | "status";
    const newSortDirection = descriptor.direction === "descending" ? "DESC" : "ASC";

    setFilters({
      sortField: newSortField,
      sortDirection: newSortDirection,
    });
  };

  // 무한 스크롤 유틸
  const { loadMoreRef } = useInfiniteScroll({
    hasNext,
    isLoading,
    onLoadMore: loadNextPage,
    rootMargin: "0px 0px 200px 0px",
  });

  // 파일 다운로드
  const onDownload = async (fileId: number) => {
    try {
      const { blob, filename } = await downloadFileById(fileId);
      downloadBlob(blob, filename);
    } catch (error) {
      console.error(error);
      errorToast("파일 다운로드 중 오류가 발생했습니다");
    }
  };

  return (
    <div className="flex h-full min-h-0 flex-col">
      {/* 테이블 영역 - 가로 스크롤 적용 */}
      <div className="border-border-secondary scrollbar-thin flex-1 overflow-auto rounded-2xl border">
        <Table
          aria-label="백업 목록"
          sortDescriptor={sortDescriptor}
          onSortChange={handleSortChange}
        >
          <Table.Header>
            <Table.Head
              id="fileId"
              label="ID"
              isRowHeader
            />
            <Table.Head
              id="worker"
              label="작업자"
            />
            <Table.Head
              id="startedAt"
              label="시작시간"
              allowsSorting
              className="min-w-40"
              isActive={isActiveSortColumn("startedAt", sortDescriptor)}
            />
            <Table.Head
              id="endedAt"
              label="종료시간"
              allowsSorting
              className="min-w-40"
              isActive={isActiveSortColumn("endedAt", sortDescriptor)}
            />
            <Table.Head
              id="status"
              label="작업상태"
              allowsSorting
              isActive={isActiveSortColumn("status", sortDescriptor)}
            />
            <Table.Head id="actions" label="다운로드" />
          </Table.Header>

          <Table.Body items={items}>
            {(item) => (
              <Table.Row id={item.id} key={item.id}>
                <Table.Cell>{item.id}</Table.Cell>
                <Table.Cell>{item.worker}</Table.Cell>
                <Table.Cell>{formatIsoToYmdHms(item.startedAt)}</Table.Cell>
                <Table.Cell>{formatIsoToYmdHms(item.endedAt)}</Table.Cell>
                <Table.Cell>
                  <StatusBadge kind="backup" value={item.status} />
                </Table.Cell>
                <Table.Cell>
                  {item.fileId && item.status === "COMPLETED" && (
                    <Button
                      color="tertiary"
                      iconLeading={Download01}
                      onClick={() => onDownload(item.fileId)}
                    />
                  )}
                </Table.Cell>
              </Table.Row>
            )}
          </Table.Body>
        </Table>

        {hasNext && <div ref={loadMoreRef} className="h-4" />}

        {/* 메시지 영역 - 스크롤 영역 밖 */}
        <div className="flex items-center justify-center py-2 text-center text-sm text-gray-600">
          {errorMessage && <span className="text-red-500">{errorMessage}</span>}
          {isLoading && <span>불러오는 중...</span>}
        </div>

        {!errorMessage && !isLoading && items.length === 0 && (
          <div className="flex h-[calc(100%-80px)] flex-1 flex-col items-center justify-center text-center">
            <span className="text-disabled">데이터 백업 정보가 없어요</span>
          </div>
        )}
      </div>
    </div>
  );
}
