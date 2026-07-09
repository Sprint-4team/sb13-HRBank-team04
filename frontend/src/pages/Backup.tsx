import { useEffect } from "react";
import { BackupTable } from "@/components/backup/BackupTable";
import { useBackupListStore } from "@/store/backupStore";
import { BackupFilterSection } from "@/components/backup/BackupFilterSection";

export default function Backup() {
  // 백업 쿼리 스토어
  const { filters, resetFilters, loadFirstPage, getLatestBackup } = useBackupListStore();

  useEffect(() => {
    resetFilters();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // store 필터 + 재조회
  useEffect(() => {
    loadFirstPage();
    getLatestBackup();
  }, [getLatestBackup, loadFirstPage, filters]);

  return (
    <>
      {/* 검색 및 백업하기 버튼 */}
      <BackupFilterSection />

      {/* 백업 목록 테이블 */}
      <BackupTable />
    </>
  );
}
