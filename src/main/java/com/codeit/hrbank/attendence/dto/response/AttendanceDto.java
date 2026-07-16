package com.codeit.hrbank.attendence.dto.response;

import com.codeit.hrbank.attendence.entity.Attendance;
import com.codeit.hrbank.attendence.enums.AttendanceType;
import java.time.LocalDate;

public record AttendanceDto(
    Long id,
    Long employeeId,
    String employeeName,
    String employeeNumber,
    String employeeEmail,
    String departmentName,
    LocalDate date,
    AttendanceType type,
    String memo
) {

  public static AttendanceDto from(Attendance attendance) {
    return new AttendanceDto(
        attendance.getId(),
        attendance.getEmployee().getId(),
        attendance.getEmployee().getName(),
        attendance.getEmployee().getEmployeeNumber(),
        attendance.getEmployee().getEmail(),
        attendance.getEmployee().getDepartment().getName(),
        attendance.getAttendanceDate(),
        attendance.getType(),
        attendance.getMemo()
    );
  }

}
