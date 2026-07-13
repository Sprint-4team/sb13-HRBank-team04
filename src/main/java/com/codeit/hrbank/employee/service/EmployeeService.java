package com.codeit.hrbank.employee.service;

import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;

public interface EmployeeService {

  EmployeeDto createEmployee(EmployeeCreateRequest employeeCreateRequest);

  EmployeeDto findEmployee(Long id);

  EmployeeDto update(Long id, EmployeeUpdateRequest request);

  void delete(Long id);
}
