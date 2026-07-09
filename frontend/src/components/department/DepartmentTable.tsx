import type { SortDescriptor } from "react-aria-components";
import { Edit01, Trash01 } from "@untitledui/icons";
import { Button } from "@/components/common/buttons/Button";
import { Table } from "@/components/common/table/Table";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import type { DepartmentDto } from "@/model/department";
import { useDepartmentListStore } from "@/store/departmentStore";
import { isActiveSortColumn } from "@/utils/sort";
import { formatDateAsKorean } from "@/utils/date";

interface DepartmentTableProps {
  onEdit: (item: DepartmentDto) => void;
  onDelete: (item: DepartmentDto) => void;
}

export function DepartmentTable({ onEdit, onDelete }: DepartmentTableProps) {
  // 부서 쿼리 스토어
  const { items, isLoading, errorMessage, hasNext, filters, setFilters, loadNextPage } =
    useDepartmentListStore();

  // 테이블 정렬 (스토어와 동기화)
  const sortDescriptor: SortDescriptor = {
    column: filters.sortField,
    direction: filters.sortDirection === "desc" ? "descending" : "ascending",
  };

  // 정렬 변경 핸들러
  const handleSortChange = (descriptor: SortDescriptor) => {
    if (!descriptor.column) return;

    const newSortField = descriptor.column as "name" | "establishedDate";
    const newSortDirection = descriptor.direction === "descending" ? "desc" : "asc";

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

  const hasNoData = !isLoading && !errorMessage && items.length === 0;

  return (
    <div className="border-border-secondary scrollbar-thin h-[692px] overflow-y-auto rounded-2xl border">
      <Table
        aria-label="부서 목록"
        sortDescriptor={sortDescriptor}
        onSortChange={handleSortChange}
      >
        <Table.Header>
          <Table.Head
            id="name"
            label="부서명"
            isRowHeader
            allowsSorting
            className="min-w-45"
            isActive={isActiveSortColumn("name", sortDescriptor)}
          />
          <Table.Head
            id="description"
            label="설명"
            className="w-full min-w-50"
          />
          <Table.Head id="employeeCount" label="인원수" />
          <Table.Head
            id="establishedDate"
            label="부서생성일"
            allowsSorting
            className="min-w-45"
            isActive={isActiveSortColumn("establishedDate", sortDescriptor)}
          />
          <Table.Head id="actions" />
        </Table.Header>

        <Table.Body items={items}>
          {(item) => (
            <Table.Row id={item.id} key={item.id}>
              <Table.Cell className="text-primary font-semibold">
                {item.name}
              </Table.Cell>
              <Table.Cell>{item.description}</Table.Cell>
              <Table.Cell>{item.employeeCount}명</Table.Cell>
              <Table.Cell>
                {formatDateAsKorean(item.establishedDate)}
              </Table.Cell>
              <Table.Cell>
                <div className="flex justify-end gap-0.5">
                  <Button
                    color="tertiary"
                    iconLeading={Trash01}
                    onClick={() => onDelete(item)}
                  />
                  <Button
                    color="tertiary"
                    iconLeading={Edit01}
                    onClick={() => onEdit(item)}
                  />
                </div>
              </Table.Cell>
            </Table.Row>
          )}
        </Table.Body>
      </Table>

      {hasNext && <div ref={loadMoreRef} className="h-4" />}

      <div className="flex flex-col items-center justify-center gap-1 py-2 text-center text-sm text-gray-600">
        {errorMessage && <span className="text-red-500">{errorMessage}</span>}
        {isLoading && <span>불러오는 중...</span>}
      </div>

      {hasNoData && (
        <div className="flex h-[calc(100%-80px)] flex-1 flex-col items-center justify-center text-center">
          <span className="text-disabled">현재 표시할 부서가 없습니다</span>
        </div>
      )}
    </div>
  );
}
