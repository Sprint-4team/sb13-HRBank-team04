package com.codeit.hrbank.employee.repository;

import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.entity.Employee;
import java.util.List;

public interface EmployeeRepositoryCustom {
  List<Employee> searchEmployees(EmployeeSearchCondition condition);
}