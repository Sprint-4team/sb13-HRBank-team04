package com.codeit.hrbank.attendence.service;

import com.codeit.hrbank.attendence.dto.request.CreateAttendanceRequest;
import com.codeit.hrbank.attendence.dto.response.AttendanceDto;
import com.codeit.hrbank.attendence.entity.Attendance;
import com.codeit.hrbank.attendence.repository.AttendanceRepository;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

  private final AttendanceRepository attendanceRepository;
  private final EmployeeRepository employeeRepository;

  @Transactional(readOnly = true)
  public List<AttendanceDto> findAttendences(LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
    }

    return attendanceRepository.findAllByDateRange(startDate, endDate).stream()
        .map(AttendanceDto::from)
        .toList();
  }

  @Transactional
  public AttendanceDto createAttendence(CreateAttendanceRequest request) {
    Employee employee = findEmployee(request.employeeId());
    validateDuplicate(null, request);

    Attendance attendance = new Attendance(
        employee, request.date(), request.type(), request.memo());
    return AttendanceDto.from(attendanceRepository.save(attendance));
  }

  @Transactional
  public AttendanceDto updateAttendence(Long id, CreateAttendanceRequest request) {
    Attendance attendance = attendanceRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("근태 내역을 찾을 수 없습니다: " + id));
    Employee employee = findEmployee(request.employeeId());

    boolean sameKey = attendance.getEmployee().getId().equals(request.employeeId())
        && attendance.getAttendanceDate().equals(request.date())
        && attendance.getType() == request.type();
    if (!sameKey) {
      validateDuplicate(id, request);
    }

    attendance.update(employee, request.date(), request.type(), request.memo());
    return AttendanceDto.from(attendance);
  }

  @Transactional
  public void deleteAttendence(Long id) {
    Attendance attendance = attendanceRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("근태 내역을 찾을 수 없습니다: " + id));
    attendanceRepository.delete(attendance);
  }

  private Employee findEmployee(Long employeeId) {
    return employeeRepository.findById(employeeId)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다: " + employeeId));
  }

  private void validateDuplicate(Long attendanceId, CreateAttendanceRequest request) {
    if (attendanceRepository.existsByEmployeeIdAndAttendanceDateAndType(
        request.employeeId(), request.date(), request.type())) {
      throw new IllegalStateException("동일한 날짜와 종류의 근태 내역이 이미 존재합니다.");
    }
  }
}
