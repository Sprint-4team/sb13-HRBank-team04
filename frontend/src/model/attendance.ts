import type { EmployeeDto } from "@/model/employee";

export type AttendanceType = "LATE" | "ANNUAL_LEAVE" | "HALF_DAY";

export interface AttendanceDto {
  id: number;
  employeeId: number;
  employeeName: string;
  employeeNumber: string;
  employeeEmail: string;
  departmentName: string;
  date: string;
  type: AttendanceType;
  memo?: string;
}

export interface AttendanceRequest {
  employeeId: number;
  date: string;
  type: AttendanceType;
  memo?: string;
}

export interface AttendanceFormValue extends AttendanceRequest {
  employee: EmployeeDto;
}
