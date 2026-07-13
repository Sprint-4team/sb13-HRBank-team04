package com.codeit.hrbank.employee.repository;

import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, EmployeeRepositoryCustom {
  boolean existsByEmail(String email);

  long countByDepartmentId(Long departmentId);

  @Query("SELECT e.employeeNumber FROM Employee e " +
      "WHERE e.employeeNumber LIKE CONCAT(:prefix, '%') " +
      "ORDER BY e.employeeNumber DESC LIMIT 1")
  String findLatestEmployeeNumber(@Param("prefix") String prefix);

  @Query("SELECT e FROM Employee e " +
      "JOIN e.department d " +
      "WHERE (:nameOrEmail IS NULL OR e.name LIKE %:nameOrEmail% OR e.email LIKE %:nameOrEmail%) " +
      "AND (:departmentName IS NULL OR d.name LIKE %:departmentName%) " +
      "AND (:position IS NULL OR e.position LIKE %:position%) " +
      "AND (:employeeNumber IS NULL OR e.employeeNumber LIKE %:employeeNumber%) " +
      "AND (:hireDateFrom IS NULL OR e.hireDate >= :hireDateFrom) " +
      "AND (:hireDateTo IS NULL OR e.hireDate <= :hireDateTo) " +
      "AND (:status IS NULL OR e.status = :status)")
  List<Employee> searchEmployees(
      @Param("nameOrEmail") String nameOrEmail,
      @Param("departmentName") String departmentName,
      @Param("position") String position,
      @Param("employeeNumber") String employeeNumber,
      @Param("hireDateFrom") LocalDate hireDateFrom,
      @Param("hireDateTo") LocalDate hireDateTo,
      @Param("status") EmployeeStatus status
  );


}





//---여기부터 빌드 코드 추가하면 주석 지우기.
