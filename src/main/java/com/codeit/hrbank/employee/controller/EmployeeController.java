package com.codeit.hrbank.employee.controller;

import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import com.codeit.hrbank.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @PostMapping
  public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreateRequest request){
    EmployeeDto employee = employeeService.createEmployee(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(employee);
  }

  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> findEmployee(@PathVariable Long id){
    EmployeeDto employee = employeeService.findEmployee(id);
    return ResponseEntity.status(HttpStatus.OK).body(employee);
  }

  @PatchMapping("/{id}")
 public ResponseEntity<EmployeeDto> update(@PathVariable Long id,
      @Valid @RequestBody EmployeeUpdateRequest request){
    EmployeeDto update = employeeService.update(id, request);
    return ResponseEntity.status(HttpStatus.OK).body(update);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    employeeService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
