import apiClient from "@/api/client";
import type { AttendanceDto, AttendanceRequest } from "@/model/attendance";

export const getAttendances = (startDate: string, endDate: string) =>
  apiClient.get<AttendanceDto[]>("/attendances", { startDate, endDate });

export const createAttendance = (request: AttendanceRequest) =>
  apiClient.post<AttendanceDto>("/attendances", request);

export const updateAttendance = (id: number, request: AttendanceRequest) =>
  apiClient.patch<AttendanceDto>(`/attendances/${id}`, request);

export const deleteAttendance = (id: number) =>
  apiClient.delete<void>(`/attendances/${id}`);
