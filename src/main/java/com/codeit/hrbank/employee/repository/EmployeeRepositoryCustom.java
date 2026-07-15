package com.codeit.hrbank.employee.repository;

import com.codeit.hrbank.employee.dto.EmployeeDistributionDto;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepositoryCustom {
  List<Employee> searchEmployees(EmployeeSearchCondition condition);

  long countEmployees(EmployeeSearchCondition condition);

  long countByCondition(EmployeeStatus status, LocalDate fromDate, LocalDate toDate);

  List<EmployeeDistributionDto> countGroupByField(String groupBy, EmployeeStatus status);

  long countHiredBefore(LocalDate asOfDate);

}