package com.codeit.hrbank.employee.service;

import com.codeit.hrbank.employee.dto.CursorPageResponseEmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {

  EmployeeDto createEmployee(EmployeeCreateRequest employeeCreateRequest, MultipartFile profile);

  EmployeeDto findEmployee(Long id);

  // EmployeeService
  CursorPageResponseEmployeeDto findEmployees(EmployeeSearchCondition condition);

  EmployeeDto update(Long id, EmployeeUpdateRequest request);

  void delete(Long id);
}
