import { useEffect, useRef, useState } from "react";
import type { SortDescriptor } from "react-aria-components";
import { SearchMd } from "@untitledui/icons";
import { getChangeLogDetails } from "@/api/history/historyApi";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import type { HistoryDto } from "@/model/history";
import { useHistoryListStore } from "@/store/historyStore";
import { useToastStore } from "@/store/toastStore";
import { formatIsoToYmdHms } from "@/utils/date";
import { isActiveSortColumn } from "@/utils/sort";
import { StatusBadge } from "../common/badges/StatusBadge";
import { Button } from "../common/buttons/Button";
import { Table } from "../common/table/Table";
import HistoryDetailModal from "./HistoryDetailModal";

const HistoryTable = () => {
  const {
    items,
    isLoading,
    errorMessage,
    hasNext,
    filters,
    setFilters,
    loadFirstPage,
    loadNextPage,
    isFiltering,
  } = useHistoryListStore();

  const [selectedHistory, setSelectedHistory] = useState<HistoryDto | null>(
    null,
  );
  const [isDetailModalOpen, setDetailModalOpen] = useState(false);

  // 테이블 정렬 (스토어와 동기화)
  const sortDescriptor: SortDescriptor = {
    column: filters.sortField,
    direction: filters.sortDirection === "desc" ? "descending" : "ascending",
  };

  // 정렬 변경 핸들러
  const handleSortChange = (descriptor: SortDescriptor) => {
    if (!descriptor.column) return;

    const newSortField = descriptor.column as "ipAddress" | "at";
    const newSortDirection = descriptor.direction === "descending" ? "desc" : "asc";

    setFilters({
      sortField: newSortField,
      sortDirection: newSortDirection,
    });
  };

  const scrollContainerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // 필터 변경을 감지할 때 실행
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollTop = 0;
    }
  }, [filters]);

  useEffect(() => {
    loadFirstPage();
  }, [loadFirstPage, filters]);

  const { errorToast } = useToastStore();
  const { loadMoreRef } = useInfiniteScroll({
    hasNext,
    isLoading: isLoading || isFiltering,
    onLoadMore: loadNextPage,
    rootMargin: "0px 0px 200px 0px",
  });

  const handleOpenDetailModal = async (history: HistoryDto) => {
    try {
      const detailData: HistoryDto = await getChangeLogDetails({
        id: history.id,
      });

      setSelectedHistory(detailData);
      setDetailModalOpen(true);
    } catch (error) {
      console.error("상세 이력을 불러오는 데 실패했습니다", error);
      errorToast("상세 이력을 불러오는 데 실패했습니다");
    }
  };

  const hasNoData = !isLoading && !errorMessage && items.length === 0;

  return (
    <div className="flex h-full min-h-0 flex-col">
      {/* 테이블 영역 - 가로 스크롤 적용 */}
      <div
        className="border-border-secondary scrollbar-thin flex-1 overflow-auto rounded-2xl border"
        ref={scrollContainerRef}
      >
        <Table
          aria-label="직원 수정 이력 목록"
          sortDescriptor={sortDescriptor}
          onSortChange={handleSortChange}
        >
          <Table.Header>
            <Table.Head id="type" label="유형" className="w-1/12" isRowHeader />
            <Table.Head
              id="employeeNumber"
              label="사원번호"
              className="w-3/12"
            />
            <Table.Head id="memo" label="변경상세내용" className="w-3/12" />
            <Table.Head
              id="ipAddress"
              label="IP주소"
              className="w-2/12"
              allowsSorting
              isActive={isActiveSortColumn("ipAddress", sortDescriptor)}
            />
            <Table.Head
              id="at"
              label="수정일"
              className="w-2/12"
              allowsSorting
              isActive={isActiveSortColumn("at", sortDescriptor)}
            />
            <Table.Head id="actions" className="w-1/12" />
          </Table.Header>
          <Table.Body items={items}>
            {(item: HistoryDto) => {
              return (
                <Table.Row key={item.id}>
                  {/* 유형 */}
                  <Table.Cell className="w-1/12">
                    <StatusBadge kind="history" value={item.type} />
                  </Table.Cell>

                  {/* 사원번호 */}
                  <Table.Cell className="w-3/12 whitespace-nowrap">
                    {item.employeeNumber}
                  </Table.Cell>

                  {/* 변경상세내용 */}
                  <Table.Cell className="w-3/12 whitespace-nowrap">
                    {item.memo}
                  </Table.Cell>

                  {/* IP주소 */}
                  <Table.Cell className="w-2/12 whitespace-nowrap">
                    {item.ipAddress}
                  </Table.Cell>

                  {/* 수정일 */}
                  <Table.Cell className="w-2/12 whitespace-nowrap">
                    {formatIsoToYmdHms(item.at)}
                  </Table.Cell>

                  {/* 액션 버튼 */}
                  <Table.Cell className="w-1/12 text-center align-middle">
                    <Button
                      color="tertiary"
                      iconLeading={SearchMd}
                      onClick={() => handleOpenDetailModal(item)}
                    />
                  </Table.Cell>
                </Table.Row>
              );
            }}
          </Table.Body>
        </Table>

        {hasNext && <div ref={loadMoreRef} className="h-4" />}

        <div className="flex flex-col items-center justify-center gap-1 py-2 text-center text-sm text-gray-600">
          {errorMessage && <span className="text-red-500">{errorMessage}</span>}
          {isLoading && <span>불러오는 중...</span>}
        </div>

        {hasNoData && (
          <div className="flex h-[calc(100%-80px)] flex-1 flex-col items-center justify-center text-center">
            <span className="text-gray-500">현재 표시할 이력이 없습니다</span>
          </div>
        )}
      </div>

      <HistoryDetailModal
        history={selectedHistory}
        isOpen={isDetailModalOpen}
        onOpenChange={setDetailModalOpen}
      />
    </div>
  );
};

export default HistoryTable;
