package com.codeit.hrbank.employee.service.basicService;

import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import com.codeit.hrbank.employee.service.EmployeeService;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicEmployeeService implements EmployeeService {

  private final EmployeeRepository employeeRepository;


  @Override
  @Transactional
  public EmployeeDto createEmployee(EmployeeCreateRequest request) {
    log.info("직원 생성 요청: email = {}", request.email());

    if (employeeRepository.existsByEmail(request.email())) {
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

    log.info("직원 생성 완료: id={}, employeeNumber={}", saved.getId(), employeeNumber);
    return EmployeeDto.from(saved);
  }


  @Override
  @Transactional(readOnly = true)
  public EmployeeDto findEmployee(Long id) {
    log.info("직원 단건 조회 요청: id = {}", id);

    Employee employee = employeeRepository.findById(id).orElseThrow(
        ()
            -> new RuntimeException("직원을 찾을 수 없습니다." + id));

    log.info("직원 단건 조회 완료: id = {}", id);
    return EmployeeDto.from(employee);
  }


  @Override
  @Transactional
  public EmployeeDto update(Long id, EmployeeUpdateRequest request) {
    log.info("직원 정보 수정 요청: id = {}", id);

    Employee employee = employeeRepository.findById(id).orElseThrow(
        () -> new NoSuchElementException("직원을 찾을 수 없습니다." + id)

    );

    if(!employee.getEmail().equals(request.email())
        &&employeeRepository.existsByEmail(request.email())){
        throw new RuntimeException("이미 사용중인 Email 입니다.");
    }


    employee.update(request.name(),request.email(),
        request.departmentId(),request.position(),
        request.hireDate(),request.status());

    log.info("직원 정보 수정 완료: id = {}", id);
    return EmployeeDto.from(employee);
  }


  @Override
  @Transactional
  public void delete(Long id) {
    log.info("직원정보 삭제 요청 id = {}", id);

    employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 id의 직원을 찾지 못 헀습니다." + id));

    // TODO: 프로필 이미지 삭제 (profileImageId를 파일 파트에 전달) - 7/11 연동
    // TODO: 삭제 이력(DELETED) 기록 - 이력 연동 시


    employeeRepository.deleteById(id);

    log.info("직원정보 삭제 완료 id = {}", id);
  }

//--헬퍼--
  private String generateEmployeeNumber(LocalDate hireDate) {

    int year = hireDate.getYear();
    String prefix = String.valueOf(year);
    String latestEmployeeNumber = employeeRepository.findLatestEmployeeNumber(prefix);
    if (latestEmployeeNumber == null) {
      return prefix + "0001";
    }
    int nextSequence = Integer.parseInt(latestEmployeeNumber.substring(4)) + 1;

    return prefix + String.format("%04d", nextSequence);

  }
}
