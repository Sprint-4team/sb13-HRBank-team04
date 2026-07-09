import type { DateValue } from "react-aria-components";
import { CalendarDate, getLocalTimeZone } from "@internationalized/date";

// DateValue → YYYY-MM-DD 문자열 변환
export const formatDateValue = (dateValue: DateValue | null) => {
  if (!dateValue) return "";

  const jsDate = dateValue.toDate(getLocalTimeZone());

  const year = jsDate.getFullYear();
  const month = String(jsDate.getMonth() + 1).padStart(2, "0");
  const day = String(jsDate.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
};

export const formatDateThisMonth = (date: Date) => {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = "01";
  return `${yyyy}-${mm}-${dd}`;
};

// RangeDateValue → YYYY-MM-DD 문자열 변환
export const formatDateRange = (
  range: {
    start: DateValue;
    end: DateValue;
  } | null,
) => {
  if (!range) return { start: "", end: "" };

  return {
    start: formatDateValue(range.start),
    end: formatDateValue(range.end),
  };
};

// RangeDateValue → YYYY-MM-DD HH:mm:ss ISO 문자열 변환
export const formatDateRangeISO = (
  range: {
    start: DateValue;
    end: DateValue;
  } | null,
) => {
  if (!range) return { start: "", end: "" };

  return {
    start: formatDateValueToIsoZ(range.start),
    end: formatDateValueToIsoZ(range.end),
  };
};

// string 날짜를 CalenderDate 객체로 변환
export const parseDateValue = (
  date: string | null | undefined,
): DateValue | null => {
  if (!date) return null;
  try {
    // "2025-04-12" 형식을 파싱
    const [year, month, day] = date.split("-").map(Number);

    // CalendarDate 객체 생성
    return new CalendarDate(year, month, day);
  } catch {
    return null;
  }
};

// YYYY-MM-DD -> 년 월 일로 문자열 변환
export const formatDateAsKorean = (dateString?: string | null): string => {
  if (!dateString) return "";

  const pattern = /^\d{4}-\d{2}-\d{2}$/;
  if (!pattern.test(dateString)) return dateString;

  const [year, month, day] = dateString.split("-");

  const yearNum = Number(year);
  const monthNum = Number(month);
  const dayNum = Number(day);

  if (isNaN(yearNum) || isNaN(monthNum) || isNaN(dayNum)) {
    return dateString;
  }

  return `${yearNum}년 ${monthNum}월 ${dayNum}일`;
};

// isoString -> YYYY-MM-DD HH:MM:SS
export const formatIsoToYmdHms = (isoString: string): string => {
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime)) {
    return "";
  }

  const pad = (n: number) => n.toString().padStart(2, "0");

  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1); // 0-based
  const day = pad(date.getDate());
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const seconds = pad(date.getSeconds());

  return `${year}년 ${month}월 ${day}일 ${hours}:${minutes}:${seconds}`;
};

// react-aria-components DateValue -> isoZ
export function formatDateValueToIsoZ(
  value: DateValue | Date | null | undefined,
): string | undefined {
  if (!value) return undefined;

  if (value instanceof Date) return value.toISOString();

  const anyValue = value as unknown as { toDate?: (timeZone?: string) => Date };

  if (typeof anyValue.toDate === "function") {
    try {
      return anyValue.toDate().toISOString();
    } catch {
      const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
      return anyValue.toDate(tz).toISOString();
    }
  }

  const v = value as unknown as { year: number; month: number; day: number };
  if (
    typeof v.year === "number" &&
    typeof v.month === "number" &&
    typeof v.day === "number"
  ) {
    return new Date(v.year, v.month - 1, v.day, 0, 0, 0, 0).toISOString();
  }

  return undefined;
}

// isoZ -> 시간 전
export const hoursAgoFromNow = (isoString?: string): number | null => {
  if (!isoString) return null;

  const now = new Date();
  const ts = Date.parse(isoString);

  if (Number.isNaN(ts)) return null;

  const diffMs = now.getTime() - ts;
  return Math.floor(diffMs / 3_600_000);
};
