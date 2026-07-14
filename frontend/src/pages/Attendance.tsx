import { useEffect, useMemo, useState } from "react";
import { createAttendance, deleteAttendance, getAttendances, updateAttendance } from "@/api/attendance/attendanceApi";
import AttendanceModal from "@/components/attendance/AttendanceModal";
import { Button } from "@/components/common/buttons/Button";
import type { AttendanceDto, AttendanceFormValue, AttendanceType } from "@/model/attendance";
import { useToastStore } from "@/store/toastStore";

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];
const TYPE: Record<AttendanceType, { label: string; color: string }> = {
  LATE: { label: "지각", color: "bg-orange-50 text-orange-700" },
  ANNUAL_LEAVE: { label: "연차", color: "bg-blue-50 text-blue-700" },
  HALF_DAY: { label: "반차", color: "bg-purple-50 text-purple-700" },
};
const dateString = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
const calendarDays = (month: Date) => {
  const first = new Date(month.getFullYear(), month.getMonth(), 1);
  const start = new Date(first);
  start.setDate(first.getDate() - first.getDay());
  return Array.from({ length: 42 }, (_, i) => { const d = new Date(start); d.setDate(start.getDate() + i); return d; });
};

export default function Attendance() {
  const [month, setMonth] = useState(new Date());
  const [items, setItems] = useState<AttendanceDto[]>([]);
  const [selectedDate, setSelectedDate] = useState(dateString(new Date()));
  const [selected, setSelected] = useState<AttendanceDto | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [notice, setNotice] = useState("");
  const { successToast, errorToast } = useToastStore();
  const days = useMemo(() => calendarDays(month), [month]);
  const start = dateString(days[0]);
  const end = dateString(days[41]);

  useEffect(() => {
    let active = true;
    setIsLoading(true); setNotice("");
    getAttendances(start, end)
      .then((data) => { if (active) setItems(data); })
      .catch(() => { if (active) { setItems([]); setNotice("근태 조회 API가 아직 연결되지 않았습니다. 화면과 직원 검색 기능은 확인할 수 있습니다."); } })
      .finally(() => { if (active) setIsLoading(false); });
    return () => { active = false; };
  }, [start, end]);

  const grouped = useMemo(() => {
    const map = new Map<string, AttendanceDto[]>();
    items.forEach((item) => map.set(item.date, [...(map.get(item.date) ?? []), item]));
    return map;
  }, [items]);
  const openCreate = (date: string) => { setSelectedDate(date); setSelected(null); setIsOpen(true); };
  const submit = async (form: AttendanceFormValue) => {
    const request = { employeeId: form.employeeId, date: form.date, type: form.type, memo: form.memo };
    setIsSaving(true);
    try {
      if (selected) {
        const updated = await updateAttendance(selected.id, request);
        setItems((old) => old.map((item) => item.id === updated.id ? updated : item));
        successToast("근태 내역을 수정했습니다.");
      } else {
        const created = await createAttendance(request);
        setItems((old) => [...old, created]);
        successToast("근태 내역을 등록했습니다.");
      }
      setIsOpen(false);
    } catch { errorToast("근태 API 연결 상태를 확인해 주세요."); }
    finally { setIsSaving(false); }
  };
  const remove = async (id: number) => {
    if (!window.confirm("이 근태 내역을 삭제하시겠습니까?")) return;
    setIsSaving(true);
    try { await deleteAttendance(id); setItems((old) => old.filter((item) => item.id !== id)); setIsOpen(false); successToast("근태 내역을 삭제했습니다."); }
    catch { errorToast("근태 내역 삭제에 실패했습니다."); }
    finally { setIsSaving(false); }
  };

  return <div className="flex min-h-0 flex-1 flex-col gap-4">
    <div className="flex items-center justify-between">
      <div><p className="text-sm text-gray-500">날짜를 클릭해 직원 근태를 등록하세요.</p><div className="mt-2 flex gap-2">{Object.values(TYPE).map((x) => <span key={x.label} className={`rounded-full px-2.5 py-1 text-xs font-semibold ${x.color}`}>{x.label}</span>)}</div></div>
      <div className="flex items-center gap-2">
        <Button color="secondary" aria-label="이전 달" onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1))}>‹</Button>
        <strong className="min-w-32 text-center text-xl">{month.getFullYear()}년 {month.getMonth() + 1}월</strong>
        <Button color="secondary" aria-label="다음 달" onClick={() => setMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1))}>›</Button>
        <Button color="secondary" onClick={() => setMonth(new Date())}>오늘</Button>
      </div>
    </div>
    {notice && <div className="rounded-lg bg-amber-50 px-4 py-2 text-sm text-amber-700">{notice}</div>}
    <div className="grid grid-cols-7 overflow-hidden rounded-xl border border-gray-200 bg-white">
      {WEEKDAYS.map((day, i) => <div key={day} className={`border-b border-gray-200 bg-gray-50 py-2.5 text-center text-sm font-semibold ${i === 0 ? "text-red-500" : i === 6 ? "text-blue-500" : "text-gray-600"}`}>{day}</div>)}
      {days.map((day) => {
        const date = dateString(day); const dayItems = grouped.get(date) ?? []; const current = day.getMonth() === month.getMonth(); const today = date === dateString(new Date());
        return <div key={date} role="button" tabIndex={0} className={`min-h-28 border-r border-b border-gray-100 p-2 outline-none hover:bg-gray-50 focus:ring-2 focus:ring-inset focus:ring-brand-300 ${current ? "bg-white" : "bg-gray-50/60"}`} onClick={() => openCreate(date)} onKeyDown={(e) => e.key === "Enter" && openCreate(date)}>
          <span className={`inline-flex size-7 items-center justify-center rounded-full text-sm ${today ? "bg-brand-600 font-bold text-white" : current ? "text-gray-800" : "text-gray-400"}`}>{day.getDate()}</span>
          <div className="mt-1 space-y-1">{dayItems.slice(0, 3).map((item) => <button key={item.id} type="button" className={`block w-full truncate rounded px-1.5 py-1 text-left text-xs font-semibold ${TYPE[item.type].color}`} onClick={(e) => { e.stopPropagation(); setSelectedDate(item.date); setSelected(item); setIsOpen(true); }}>{item.employeeName} · {TYPE[item.type].label}</button>)}{dayItems.length > 3 && <p className="text-xs text-gray-500">+{dayItems.length - 3}건</p>}</div>
        </div>;
      })}
    </div>
    {isLoading && <p className="text-center text-sm text-gray-500">근태 내역을 불러오는 중입니다.</p>}
    <AttendanceModal isOpen={isOpen} date={selectedDate} attendance={selected} isSaving={isSaving} onOpenChange={setIsOpen} onSubmit={submit} onDelete={remove} />
  </div>;
}
