import { useEffect, useRef, useState } from "react";
import type { SortDescriptor } from "react-aria-components";
import { Edit01, Trash01 } from "@untitledui/icons";
import { deleteEmployee } from "@/api/employee/employeeApi";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import type { EmployeeDto } from "@/model/employee";
import { useEmployeeListStore } from "@/store/employeeStore";
import { useToastStore } from "@/store/toastStore";
import { formatDateAsKorean } from "@/utils/date";
import { buildProfileUrl } from "@/utils/profileUrl";
import { isActiveSortColumn } from "@/utils/sort";
import { AvatarLabelGroup } from "../common/avatar/AvatarLabelGroup";
import { StatusBadge } from "../common/badges/StatusBadge";
import { Button } from "../common/buttons/Button";
import ConfirmModal from "../common/modals/ConfirmModal";
import { Table } from "../common/table/Table";
import CreateUpdateEmployeeModal from "./CreateUpdateEmployeeModal";

const EmployeeTable = () => {
  const {
    items,
    isLoading,
    errorMessage,
    filters,
    setFilters,
    hasNext,
    loadFirstPage,
    loadNextPage,
  } = useEmployeeListStore();

  // 테이블 정렬 (스토어와 동기화)
  const sortDescriptor: SortDescriptor = {
    column: filters.sortField,
    direction: filters.sortDirection === "desc" ? "descending" : "ascending",
  };

  // 정렬 변경 핸들러
  const handleSortChange = (descriptor: SortDescriptor) => {
    if (!descriptor.column) return;

    const newSortField = descriptor.column as "name" | "employeeNumber" | "hireDate";
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
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [targetEmployeeName, setTargetEmployeeName] = useState<string>("");
  const [targetEmployeeId, setTargetEmployeeId] = useState<number | null>(null);
  // 수정 모달
  const [isUpdateModalOpen, setUpdateModalOpen] = useState(false);
  const [updatingEmployee, setUpdatingEmployee] = useState<EmployeeDto | null>(
    null,
  );
  const { successToast, errorToast } = useToastStore();

  useEffect(() => {
    loadFirstPage();
  }, [loadFirstPage, filters]);

  const { loadMoreRef } = useInfiniteScroll({
    hasNext,
    isLoading,
    onLoadMore: loadNextPage,
    rootMargin: "0px 0px 200px 0px",
  });

  // 수정 모달 핸들러
  const handleOpenUpdateModal = (employee: EmployeeDto) => {
    setUpdatingEmployee(employee);
    setUpdateModalOpen(true);
  };

  // 삭제 모달 핸들러
  const handleOpenConfirmModal = (employeeId: number, employeeName: string) => {
    setTargetEmployeeId(employeeId);
    setTargetEmployeeName(employeeName);
    setDeleteModalOpen(true);
  };

  const handleDelete = async () => {
    if (targetEmployeeId == null) return;

    try {
      await deleteEmployee(targetEmployeeId);
      await loadFirstPage();
      successToast("직원이 삭제되었습니다");
    } catch (error) {
      if (process.env.NODE_ENV === "development") {
        console.error("직원 삭제 실패", error);
      }
      errorToast("직원 삭제에 실패하였습니다");
    } finally {
      setDeleteModalOpen(false);
      setTargetEmployeeId(null);
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
          aria-label="직원 목록"
          sortDescriptor={sortDescriptor}
          onSortChange={handleSortChange}
        >
          <Table.Header>
            <Table.Head
              id="name"
              label="이름"
              isRowHeader
              allowsSorting
              isActive={isActiveSortColumn("name", sortDescriptor)}
            />
            <Table.Head
              id="employeeNumber"
              label="사원번호"
              allowsSorting
              isActive={isActiveSortColumn("employeeNumber", sortDescriptor)}
            />
            <Table.Head id="departmentName" label="부서명" />
            <Table.Head id="position" label="직함" />
            <Table.Head
              id="hireDate"
              label="입사일"
              allowsSorting
              isActive={isActiveSortColumn("hireDate", sortDescriptor)}
            />
            <Table.Head id="status" label="재직상태" />
            <Table.Head id="actions" />
          </Table.Header>
          <Table.Body items={items}>
            {(item) => {
              return (
                <Table.Row id={item.id} key={item.id}>
                  {/* 이름 + 사번 */}
                  <Table.Cell>
                    <AvatarLabelGroup
                      size="md"
                      src={buildProfileUrl(item.profileImageId)}
                      alt={`${item.name}의 프로필`}
                      title={item.name}
                      subtitle={item.email}
                    />
                  </Table.Cell>

                  {/* 사원번호 */}
                  <Table.Cell className="whitespace-nowrap">
                    {item.employeeNumber}
                  </Table.Cell>

                  {/* 부서명 */}
                  <Table.Cell className="whitespace-nowrap">
                    {item.departmentName}
                  </Table.Cell>

                  {/* 직함 */}
                  <Table.Cell className="whitespace-nowrap">
                    {item.position}
                  </Table.Cell>

                  {/* 입사일 */}
                  <Table.Cell className="whitespace-nowrap">
                    {formatDateAsKorean(item.hireDate)}
                  </Table.Cell>

                  {/* 상태 */}
                  <Table.Cell>
                    <StatusBadge kind="employment" value={item.status} />
                  </Table.Cell>

                  {/* 액션 버튼 */}
                  <Table.Cell className="px-4">
                    <div className="flex justify-end gap-0.5">
                      <Button
                        color="tertiary"
                        iconLeading={Trash01}
                        onClick={() =>
                          handleOpenConfirmModal(item.id, item.name)
                        }
                      />
                      <Button
                        color="tertiary"
                        iconLeading={Edit01}
                        onClick={() => handleOpenUpdateModal(item)}
                      />
                    </div>
                  </Table.Cell>
                </Table.Row>
              );
            }}
          </Table.Body>
        </Table>
        {hasNoData && (
          <div className="flex h-[calc(100%-80px)] flex-1 flex-col items-center justify-center text-center">
            <span className="text-gray-500">현재 표시할 직원이 없습니다</span>
          </div>
        )}
        {hasNext && <div ref={loadMoreRef} className="h-4" />}
        <div className="flex flex-col items-center justify-center gap-1 py-2 text-center text-sm text-gray-600">
          {errorMessage && <span className="text-red-500">{errorMessage}</span>}
          {isLoading && <span>불러오는 중...</span>}
        </div>
        {hasNoData && (
          <div className="flex h-[calc(100%-80px)] flex-1 flex-col items-center justify-center text-center">
            <span className="text-gray-500">현재 표시할 직원이 없습니다</span>
          </div>
        )}
      </div>

      {/* 수정 모달 */}
      <CreateUpdateEmployeeModal
        employee={updatingEmployee}
        isOpen={isUpdateModalOpen}
        onOpenChange={setUpdateModalOpen}
      />
      {/* 삭제 모달 */}
      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        onConfirm={handleDelete}
      >
        <p>
          ‘{targetEmployeeName}’ 직원을 삭제할까요?
          <br />
          삭제 후에는 되돌릴 수 없어요
        </p>
      </ConfirmModal>
    </div>
  );
};

export default EmployeeTable;
