package com.codeit.hrbank.employee.repository;

import com.codeit.hrbank.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
  boolean existsByEmail(String email);

  @Query("SELECT e.employeeNumber FROM Employee e " +
      "WHERE e.employeeNumber LIKE CONCAT(:prefix, '%') " +
      "ORDER BY e.employeeNumber DESC LIMIT 1")
  String findLatestEmployeeNumber(@Param("prefix") String prefix);

}
