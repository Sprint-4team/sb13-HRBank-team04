package com.codeit.hrbank.employee.service;

import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;

public interface EmployeeService {
  EmployeeDto createEmployee(EmployeeCreateRequest employeeCreateRequest);

}
