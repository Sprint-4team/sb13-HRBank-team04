package com.codeit.hrbank.attendance.entity;

import com.codeit.hrbank.attendance.enums.AttendanceType;
import com.codeit.hrbank.employee.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attendances")
public class Attendance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "employee_id", nullable = false)
  private Employee employee;

  @Column(nullable = false)
  private LocalDate attendanceDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AttendanceType type;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant updatedAt;

  private String memo;

  public Attendance(Employee employee, LocalDate attendanceDate,
      AttendanceType type, String memo) {
    this.employee = employee;
    this.attendanceDate = attendanceDate;
    this.type = type;
    this.memo = memo;
    this.createdAt = Instant.now();
  }

  public void update(Employee employee, LocalDate attendanceDate,
      AttendanceType type, String memo) {
    this.employee = employee;
    this.attendanceDate = attendanceDate;
    this.type = type;
    this.memo = memo;
    this.updatedAt = Instant.now();
  }
}
