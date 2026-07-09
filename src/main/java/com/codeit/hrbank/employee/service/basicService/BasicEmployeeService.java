package com.codeit.hrbank.employee.service.basicService;

import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import com.codeit.hrbank.employee.service.EmployeeService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicEmployeeService implements EmployeeService {

  private final EmployeeRepository employeeRepository;


  @Override
  public EmployeeDto createEmployee(EmployeeCreateRequest request) {
    if(employeeRepository.existsByEmail(request.email())){
      throw new RuntimeException("이미 존재하는 Email입니다." + request.email());
      //예외는 추후 커스텀을 고려해보겠습니다.
    }

    String employeeNumber = generateEmployeeNumber(request.hireDate());

    Employee employee = Employee.builder()
        .name(request.name())
        .email(request.email())
        .employeeNumber(employeeNumber)
        .position(request.position())
        .hireDate(request.hireDate())
        .status(EmployeeStatus.ACTIVE)
        .departmentId(request.departmentId())
        .profileImageId(null)
        .build();

    Employee saved = employeeRepository.save(employee);

     return EmployeeDto.from(saved);
  }


  private String generateEmployeeNumber(LocalDate hireDate){
    int year = hireDate.getYear();
    String prefix = String.valueOf(year);
    String latestEmployeeNumber = employeeRepository.findLatestEmployeeNumber(prefix);
    if(latestEmployeeNumber == null){
      return prefix + "0001";
    }
    int nextSequence = Integer.parseInt(latestEmployeeNumber.substring(4))+1;
    return prefix + String.format("%04d", nextSequence);

  }
}
