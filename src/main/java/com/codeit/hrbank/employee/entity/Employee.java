package com.codeit.hrbank.employee.entity;

import com.codeit.hrbank.department.Department;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.codeit.hrbank.file.entity.File;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class Employee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreatedDate
  private Instant createdAt;

  @Column(name = "updated_at")
  @LastModifiedDate
  private Instant updatedAt;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "email", nullable = false, length = 100, unique = true)
  private String email;

  @Column(name = "employee_number", nullable = false, length = 50, unique = true, updatable = false)
  private String employeeNumber;

  @Column(name = "position", nullable = false, length = 100)
  private String position;

  @Column(name = "hire_date", nullable = false)
  private LocalDate hireDate;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private EmployeeStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_image_id")
  private File profileImage;

  public void update(String name, String email, Department department,
      String position, LocalDate hireDate, EmployeeStatus status) {
    this.name = name;
    this.email = email;
    this.department = department;
    this.position = position;
    this.hireDate = hireDate;
    this.status = status;
  }

  public void updateProfileImage(File profileImage) {
    this.profileImage = profileImage;
  }
}