package com.codeit.hrbank.employee.service.basicService;

import com.codeit.hrbank.department.Department;
import com.codeit.hrbank.department.DepartmentNotFoundException;
import com.codeit.hrbank.department.DepartmentRepository;
import com.codeit.hrbank.employee.dto.CursorPageResponseEmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import com.codeit.hrbank.employee.service.EmployeeService;
import com.codeit.hrbank.file.entity.File;
import com.codeit.hrbank.file.service.FileService;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicEmployeeService implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final FileService fileService;


  @Override
  @Transactional
  public EmployeeDto createEmployee(EmployeeCreateRequest request, MultipartFile profile) {
    log.info("직원 생성 요청: email = {}", request.email());

    if (employeeRepository.existsByEmail(request.email())) {
      throw new RuntimeException("이미 존재하는 Email입니다." + request.email());
    }

    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));

    String employeeNumber = generateEmployeeNumber(request.hireDate());

    File profileImage = null;
    if (profile != null && !profile.isEmpty()) {
      profileImage = fileService.createFile(profile);   // ← createFile로 연결
    }

    Employee employee = Employee.builder()
        .name(request.name())
        .email(request.email())
        .employeeNumber(employeeNumber)
        .position(request.position())
        .hireDate(request.hireDate())
        .status(EmployeeStatus.ACTIVE)
        .department(department)
        .profileImage(profileImage)
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
  @Transactional(readOnly = true)
  public CursorPageResponseEmployeeDto findEmployees(EmployeeSearchCondition condition) {
    log.info("직원 목록 조회 요청: condition = {}", condition);

    int size = condition.size() > 0 ? condition.size() : 10;
    List<Employee> employees = employeeRepository.searchEmployees(condition);

    boolean hasNext = employees.size() > size;
    List<Employee> content = hasNext ? employees.subList(0, size) : employees;

    List<EmployeeDto> dtoList = content.stream()
        .map(EmployeeDto::from)
        .toList();

    String nextCursor = null;
    Long nextIdAfter = null;
    if (!content.isEmpty()) {
      Employee last = content.get(content.size() - 1);
      nextIdAfter = last.getId();
      String sortField = condition.sortField() != null ? condition.sortField() : "name";
      nextCursor = switch (sortField) {
        case "hireDate" -> last.getHireDate().toString();
        case "employeeNumber" -> last.getEmployeeNumber();
        default -> last.getName();
      };
    }

    log.info("직원 목록 조회 완료: count = {}", dtoList.size());

    return new CursorPageResponseEmployeeDto(
        dtoList, nextCursor, nextIdAfter, dtoList.size(), null, hasNext
    );
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

    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));

    employee.update(request.name(),request.email(),
        department,request.position(),
        request.hireDate(),request.status());

    log.info("직원 정보 수정 완료: id = {}", id);
    return EmployeeDto.from(employee);
  }


  @Override
  @Transactional
  public void delete(Long id) {
    log.info("직원정보 삭제 요청 id = {}", id);

    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 id의 직원을 찾지 못했습니다." + id));

    if (employee.getProfileImage() != null) {
      fileService.deleteFile(employee.getProfileImage().getId());
    }

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
