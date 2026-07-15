package com.codeit.hrbank.employee.service;

import com.codeit.hrbank.employee.dto.CursorPageResponseEmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeDistributionDto;
import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeTrendDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {

  EmployeeDto createEmployee(EmployeeCreateRequest employeeCreateRequest, MultipartFile profile, String ipAddress);

  EmployeeDto findEmployee(Long id);

  // EmployeeService
  CursorPageResponseEmployeeDto findEmployees(EmployeeSearchCondition condition);

  long countEmployees(EmployeeStatus status, LocalDate fromDate, LocalDate toDate);

  List<EmployeeDistributionDto> getDistribution(String groupBy, EmployeeStatus status);

  List<EmployeeTrendDto> getTrend(LocalDate from, LocalDate to, String unit);

  EmployeeDto update(Long id, EmployeeUpdateRequest request, MultipartFile profile, String ipAddress);

  void delete(Long id, String ipAddress);
}
