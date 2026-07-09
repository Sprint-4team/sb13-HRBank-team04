import type { HistoryDto } from "@/model/history";
import { formatIsoToYmdHms } from "@/utils/date";
import { buildProfileUrl } from "@/utils/profileUrl";
import { AvatarLabelGroup } from "../common/avatar/AvatarLabelGroup";
import { StatusBadge } from "../common/badges/StatusBadge";
import { BaseModal } from "../common/modals/BaseModal";

const TARGET_MAP = {
  hireDate: "입사일",
  name: "이름",
  position: "직함",
  department: "부서",
  email: "이메일",
  employeeNumber: "사번",
  status: "상태",
};

interface HIstoryDetailModalProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  history: HistoryDto | null;
}

const HistoryDetailModal = ({
  isOpen,
  onOpenChange,
  history,
}: HIstoryDetailModalProps) => {
  const handleClose = (boolean: boolean) => {
    onOpenChange(boolean);
  };

  if (!history) return null;
  const diffs = history.diffs || [];

  const renderValue = (
    fieldName: string,
    value: unknown,
    _isNewValue: boolean,
  ) => {
    // 값이 없거나 '-' 일 경우
    if (
      value === null ||
      value === undefined ||
      String(value).trim() === "" ||
      value === "-"
    ) {
      return <span>-</span>;
    }

    // fieldName이 '상태'나 '유형'일 경우 StatusBadge 사용
    if (fieldName === "status") {
      if (typeof value === "string") {
        return <StatusBadge kind="employment" value={value} />;
      }
    }

    // 일반 텍스트의 경우
    if (typeof value === "string") {
      return <span>{String(value)}</span>;
    }
  };

  const profileImageSrc = history.profileImageId
    ? buildProfileUrl(history.profileImageId)
    : undefined;

  return (
    <div>
      <BaseModal
        title="수정 이력 상세"
        isOpen={isOpen}
        onOpenChange={() => handleClose(false)}
        className="w-[600px] max-w-none"
      >
        <AvatarLabelGroup
          size="md"
          src={profileImageSrc}
          alt={`프로필`}
          title={history.employeeName}
          subtitle={history.employeeNumber}
        />
        <hr className="border-border-secondary" />
        {/* 2. 유형, 일시, IP 주소 그룹 */}
        <div className="mb-4 flex flex-col gap-3 text-sm">
          {/* 유형 */}
          <div className="flex gap-1">
            <label className="text-quaternary w-14">유형</label>
            <div className="flex items-center gap-1">
              <StatusBadge kind="history" value={history?.type || ""} />

              {history?.memo && history?.memo.trim() !== "" && (
                <>
                  <span className="whitespace-nowrap text-gray-400">•</span>
                  <span className="text-primary-900 whitespace-nowrap">
                    {history?.memo}
                  </span>
                </>
              )}
            </div>
          </div>
          {/* 일시 */}
          <div className="flex gap-1">
            <label className="text-quaternary w-14">일시</label>
            <p className="whitespace-nowrap text-gray-900">
              {/* {formatDateAsKorean(history.atFrom)} */}
              {formatIsoToYmdHms(history?.at || "")}
            </p>
          </div>
          {/* IP 주소 */}
          <div className="flex gap-1">
            <label className="text-quaternary w-14">IP 주소</label>
            <p className="whitespace-nowrap text-gray-900">
              {history?.ipAddress}
            </p>
          </div>
          <hr className="border-border-secondary" />
        </div>
        {/* 변경 상세 내용 컨테이너 (테이블) */}
        <div className="border-border-secondary flex w-full flex-col gap-2 rounded-xl border pt-3">
          <h2 className="text-md px-5 font-semibold">변경 상세 내용</h2>
          {diffs.length > 0 ? (
            // 데이터 있으면
            <div className="flex flex-col gap-5 p-5">
              {/* 테이블 헤더 */}
              <div className="text-quaternary flex items-center justify-end gap-5 text-left text-sm">
                <div className="w-1/5" />
                <div className="w-2/5">변경 전</div>
                <div className="w-2/5">변경 후</div>
              </div>
              {/* 테이블 바디 */}
              {diffs.map((diffs, index) => (
                <div
                  key={index}
                  className="flex items-center gap-5 text-left text-sm"
                >
                  <div className="text-quaternary w-1/5">
                    {TARGET_MAP[diffs.propertyName as keyof typeof TARGET_MAP]}
                  </div>
                  <div className="w-2/5">
                    {renderValue(diffs.propertyName, diffs.before, false)}
                  </div>
                  <div className="w-2/5">
                    {renderValue(diffs.propertyName, diffs.after, true)}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            // 데이터 없을 경우
            <div className="py-9 text-center">
              변경 이력 상세 내용이 없습니다
            </div>
          )}{" "}
        </div>
      </BaseModal>
    </div>
  );
};

export default HistoryDetailModal;
