import { useEffect, useState } from "react";
import { getEmployees } from "@/api/employee/employeeApi";
import { Button } from "@/components/common/buttons/Button";
import { BaseModal } from "@/components/common/modals/BaseModal";
import type { AttendanceDto, AttendanceFormValue, AttendanceType } from "@/model/attendance";
import type { EmployeeDto } from "@/model/employee";

const TYPES: { value: AttendanceType; label: string }[] = [
  { value: "LATE", label: "지각" },
  { value: "ANNUAL_LEAVE", label: "연차" },
  { value: "HALF_DAY", label: "반차" },
];

interface Props {
  isOpen: boolean;
  date: string;
  attendance: AttendanceDto | null;
  isSaving: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (value: AttendanceFormValue) => Promise<void>;
  onDelete: (id: number) => Promise<void>;
}

export default function AttendanceModal(props: Props) {
  const { isOpen, date, attendance, isSaving, onOpenChange, onSubmit, onDelete } = props;
  const [selectedDate, setSelectedDate] = useState(date);
  const [type, setType] = useState<AttendanceType>("LATE");
  const [memo, setMemo] = useState("");
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<EmployeeDto[]>([]);
  const [employee, setEmployee] = useState<EmployeeDto | null>(null);
  const [message, setMessage] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    setSelectedDate(attendance?.date ?? date);
    setType(attendance?.type ?? "LATE");
    setMemo(attendance?.memo ?? "");
    setKeyword("");
    setResults([]);
    setMessage("");
    setEmployee(attendance ? {
      id: attendance.employeeId,
      name: attendance.employeeName,
      email: attendance.employeeEmail,
      employeeNumber: attendance.employeeNumber,
      departmentId: 0,
      departmentName: attendance.departmentName,
      position: "",
      hireDate: "",
      status: "ACTIVE",
      profileImageId: null,
    } : null);
  }, [attendance, date, isOpen]);

  const search = async () => {
    if (!keyword.trim()) return setMessage("이름 또는 이메일을 입력해 주세요.");
    setIsSearching(true);
    setMessage("");
    try {
      const response = await getEmployees({ nameOrEmail: keyword.trim(), size: 10 });
      setResults(response.content);
      if (!response.content.length) setMessage("검색 결과가 없습니다.");
    } catch {
      setMessage("직원 검색 API 연결 상태를 확인해 주세요.");
    } finally {
      setIsSearching(false);
    }
  };

  const save = async () => {
    if (!employee) return setMessage("근태를 등록할 직원을 선택해 주세요.");
    await onSubmit({ employee, employeeId: employee.id, date: selectedDate, type, memo: memo.trim() || undefined });
  };

  const field = "h-10 w-full rounded-lg border border-gray-300 bg-white px-3 text-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-100";

  return (
    <BaseModal
      isOpen={isOpen}
      onOpenChange={onOpenChange}
      title={attendance ? "근태 내역 수정" : "근태 내역 등록"}
      className="max-w-lg"
      footer={<>
        {attendance && <Button color="primary-destructive" isDisabled={isSaving} onClick={() => onDelete(attendance.id)}>삭제</Button>}
        <div className="ml-auto flex gap-2">
          <Button color="secondary" onClick={() => onOpenChange(false)}>취소</Button>
          <Button isLoading={isSaving} onClick={save}>{attendance ? "수정" : "등록"}</Button>
        </div>
      </>}
    >
      <label className="block space-y-1.5">
        <span className="text-sm font-semibold text-gray-700">날짜</span>
        <input type="date" className={field} value={selectedDate} onChange={(e) => setSelectedDate(e.target.value)} />
      </label>
      <div className="space-y-2">
        <span className="text-sm font-semibold text-gray-700">직원</span>
        {employee ? (
          <div className="flex items-center justify-between rounded-lg border border-brand-200 bg-brand-50 p-3">
            <div><p className="font-semibold text-gray-900">{employee.name}</p><p className="text-sm text-gray-600">{employee.email} · {employee.departmentName || "부서 없음"}</p></div>
            {!attendance && <button type="button" className="text-sm font-semibold text-brand-700" onClick={() => setEmployee(null)}>변경</button>}
          </div>
        ) : <>
          <div className="flex gap-2">
            <input className={field} value={keyword} placeholder="이름 또는 이메일 검색" onChange={(e) => setKeyword(e.target.value)} onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); void search(); } }} />
            <Button color="secondary" isLoading={isSearching} onClick={search}>검색</Button>
          </div>
          {message && <p className="text-sm text-gray-500">{message}</p>}
          {!!results.length && <ul className="max-h-44 overflow-auto rounded-lg border border-gray-200">{results.map((item) => <li key={item.id}><button type="button" className="w-full border-b border-gray-100 px-3 py-2 text-left hover:bg-gray-50" onClick={() => { setEmployee(item); setResults([]); }}><strong className="block text-sm">{item.name}</strong><span className="text-xs text-gray-500">{item.email} · {item.departmentName || "부서 없음"}</span></button></li>)}</ul>}
        </>}
      </div>
      <fieldset className="space-y-2">
        <legend className="text-sm font-semibold text-gray-700">근태 종류</legend>
        <div className="grid grid-cols-3 gap-2">{TYPES.map((item) => <label key={item.value} className={`cursor-pointer rounded-lg border p-2.5 text-center text-sm font-semibold ${type === item.value ? "border-brand-500 bg-brand-50 text-brand-700" : "border-gray-200 text-gray-600"}`}><input className="sr-only" type="radio" checked={type === item.value} onChange={() => setType(item.value)} />{item.label}</label>)}</div>
      </fieldset>
      <label className="block space-y-1.5"><span className="text-sm font-semibold text-gray-700">메모 (선택)</span><textarea className="min-h-20 w-full resize-none rounded-lg border border-gray-300 p-3 text-sm outline-none focus:border-brand-500" maxLength={200} value={memo} onChange={(e) => setMemo(e.target.value)} /></label>
    </BaseModal>
  );
}
