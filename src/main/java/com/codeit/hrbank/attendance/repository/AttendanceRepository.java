package com.codeit.hrbank.attendance.repository;

import com.codeit.hrbank.attendance.entity.Attendance;
import com.codeit.hrbank.attendance.enums.AttendanceType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  @Query("""
      SELECT a FROM Attendance a
      JOIN FETCH a.employee e
      JOIN FETCH e.department
      WHERE a.attendanceDate BETWEEN :startDate AND :endDate
      ORDER BY a.attendanceDate ASC, a.id ASC
      """)
  List<Attendance> findAllByDateRange(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate
  );

  boolean existsByEmployeeIdAndAttendanceDateAndType(
      Long employeeId, LocalDate attendanceDate, AttendanceType type);
}
