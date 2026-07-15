package com.codeit.hrbank.employee.controller;

import com.codeit.hrbank.employee.dto.CursorPageResponseEmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeDistributionDto;
import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeTrendDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.codeit.hrbank.employee.service.EmployeeService;
import com.codeit.hrbank.global.util.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EmployeeDto> createEmployee(
      @RequestPart("employee") @Valid EmployeeCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      HttpServletRequest httpServletRequest
  ) {
    String ipAddress = CommonUtils.getRemoteIp(httpServletRequest);
    EmployeeDto employee = employeeService.createEmployee(request, profile, ipAddress);
    return ResponseEntity.status(HttpStatus.CREATED).body(employee);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> findEmployee(@PathVariable Long id){
    EmployeeDto employee = employeeService.findEmployee(id);
    return ResponseEntity.status(HttpStatus.OK).body(employee);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseEmployeeDto> findEmployees(
      @RequestParam(required = false) String nameOrEmail,
      @RequestParam(required = false) String departmentName,
      @RequestParam(required = false) String position,
      @RequestParam(required = false) String employeeNumber,
      @RequestParam(required = false) LocalDate hireDateFrom,
      @RequestParam(required = false) LocalDate hireDateTo,
      @RequestParam(required = false) EmployeeStatus status,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortField,
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    EmployeeSearchCondition condition = new EmployeeSearchCondition(
        nameOrEmail, departmentName, position, employeeNumber,
        hireDateFrom, hireDateTo, status,
        cursor, idAfter, size, sortField,
        Sort.Direction.fromString(sortDirection)
    );

    return ResponseEntity.ok(employeeService.findEmployees(condition));
  }

  @GetMapping("/count")
  public ResponseEntity<Long> countEmployees(
      @RequestParam(required = false) EmployeeStatus status,
      @RequestParam(required = false) LocalDate fromDate,
      @RequestParam(required = false) LocalDate toDate
  ) {
    long count = employeeService.countEmployees(status, fromDate, toDate);
    return ResponseEntity.status(HttpStatus.OK).body(count);
  }

  @GetMapping("/stats/distribution")
  public ResponseEntity<List<EmployeeDistributionDto>> getDistribution(
      @RequestParam(defaultValue = "department") String groupBy,
      @RequestParam(required = false) EmployeeStatus status
  ) {
    List<EmployeeDistributionDto> distribution = employeeService.getDistribution(groupBy, status);
    return ResponseEntity.status(HttpStatus.OK).body(distribution);
  }

  @GetMapping("/stats/trend")
  public ResponseEntity<List<EmployeeTrendDto>> getTrend(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to,
      @RequestParam(defaultValue = "month") String unit
  ){
    List<EmployeeTrendDto> trend = employeeService.getTrend(from, to, unit);
    return ResponseEntity.status(HttpStatus.OK).body(trend);
  }


  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EmployeeDto> update(
      @PathVariable Long id,
      @RequestPart("employee") @Valid EmployeeUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      HttpServletRequest httpServletRequest
  ) {
    String ipAddress = CommonUtils.getRemoteIp(httpServletRequest);
    EmployeeDto updated = employeeService.update(id, request, profile, ipAddress);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest httpServletRequest){
    String ipAddress = CommonUtils.getRemoteIp(httpServletRequest);
    employeeService.delete(id, ipAddress);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
