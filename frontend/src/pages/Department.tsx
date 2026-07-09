import { deleteDepartment } from "@/api/department/departmentApi";
import ConfirmModal from "@/components/common/modals/ConfirmModal";
import { DepartmentFilterSection } from "@/components/department/DepartmentFilterSection";
import DepartmentModal from "@/components/department/DepartmentModal";
import { DepartmentTable } from "@/components/department/DepartmentTable";
import type { DepartmentDto } from "@/model/department";
import { useDepartmentListStore } from "@/store/departmentStore";
import { useToastStore } from "@/store/toastStore";
import axios from "axios";
import { useEffect, useState } from "react";

export default function Department() {
  // 부서 쿼리 스토어
  const { filters, loadFirstPage } = useDepartmentListStore();
  const { successToast, errorToast } = useToastStore();

  const [isModalOpen, setIsModalOpen] = useState(false); // 모달 상태
  const [isConfirmOpen, setIsConfirmOpen] = useState(false); // 모달 상태
  const [selected, setSelected] = useState<DepartmentDto | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DepartmentDto | null>(null);

  // store 필터 + 재조회
  useEffect(() => {
    loadFirstPage();
  }, [loadFirstPage, filters]);

  // 부서 추가 모달 열기
  const onAddClick = () => {
    setSelected(null);
    setIsModalOpen(true);
  };

  // 부서 수정 모달 열기
  const onEditClick = (item: DepartmentDto) => {
    setSelected(item);
    setIsModalOpen(true);
  };

  // 부서 삭제 확인 모달 열기
  const onDeleteClick = (item: DepartmentDto) => {
    setDeleteTarget(item);
    setIsConfirmOpen(true);
  };

  // 부서 삭제
  const handleDelete = async () => {
    if (!deleteTarget) return;

    try {
      await deleteDepartment(deleteTarget?.id);
      loadFirstPage();

      successToast("부서가 삭제되었습니다");
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message as string | undefined;
        const details = error.response?.data?.details as string;

        if (status === 400 && message === "IllegalStateException") {
          errorToast(details);
          return;
        }
      }
      console.log(error);
    } finally {
      setDeleteTarget(null);
      setIsConfirmOpen(false);
    }
  };

  return (
    <>
      {/* 필터 및 버튼 */}
      <DepartmentFilterSection onAdd={onAddClick} />

      {/* 부서 목록 테이블 */}
      <DepartmentTable onEdit={onEditClick} onDelete={onDeleteClick} />

      {/* Modal */}
      <DepartmentModal
        key={selected?.id || "new"}
        isOpen={isModalOpen}
        onOpenChange={setIsModalOpen}
        department={selected}
      />

      {/* Confirm Modal */}
      <ConfirmModal isOpen={isConfirmOpen} onClose={() => setIsConfirmOpen(false)} onConfirm={handleDelete}>
        <p>
          '{`${deleteTarget?.name}`}' 부서를 삭제할까요? <br />
          삭제 후에는 되돌릴 수 없어요
        </p>
      </ConfirmModal>
    </>
  );
}
