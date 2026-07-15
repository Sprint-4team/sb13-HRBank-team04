package com.codeit.hrbank.employee.service.basicService;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.dto.DiffDto;
import com.codeit.hrbank.changelog.service.ChangeLogService;
import com.codeit.hrbank.department.entity.Department;
import com.codeit.hrbank.department.exception.DepartmentNotFoundException;
import com.codeit.hrbank.department.repository.DepartmentRepository;
import com.codeit.hrbank.employee.dto.CursorPageResponseEmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeDistributionDto;
import com.codeit.hrbank.employee.dto.EmployeeDto;
import com.codeit.hrbank.employee.dto.EmployeeTrendDto;
import com.codeit.hrbank.employee.dto.request.EmployeeCreateRequest;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.dto.request.EmployeeUpdateRequest;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.codeit.hrbank.employee.exception.EmailDuplicateException;
import com.codeit.hrbank.employee.exception.EmployeeNotFoundException;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import com.codeit.hrbank.employee.service.EmployeeService;
import com.codeit.hrbank.file.entity.File;
import com.codeit.hrbank.file.service.FileService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
  private final ChangeLogService changeLogService;


  @Override
  @Transactional
  public EmployeeDto createEmployee(EmployeeCreateRequest request, MultipartFile profile, String ipAddress) {
    log.info("직원 생성 요청: email = {}", request.email());

    if (employeeRepository.existsByEmail(request.email())) {
      throw new EmailDuplicateException(request.email());
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

    Employee saved;
    try{
      saved = employeeRepository.save(employee);
      employeeRepository.flush();
    } catch (DataIntegrityViolationException e) {
      if(profileImage != null) {
        fileService.deleteFile(profileImage.getId());
      }

      throw new EmailDuplicateException(request.email());
    }

    // =========================
    // ChangeLog 연동 작업
    // 직원 생성 이력 저장
    changeLogService.saveChangeLog(
            EmployeeChangeType.CREATED,
            saved,
            saved.getEmployeeNumber(),
            request.memo(),
            ipAddress,
            List.of()
    ); // =========================

    log.info("직원 생성 완료: id={}, employeeNumber={}", saved.getId(), employeeNumber);
    return EmployeeDto.from(saved);
  }


  @Override
  @Transactional(readOnly = true)
  public EmployeeDto findEmployee(Long id) {
    log.info("직원 단건 조회 요청: id = {}", id);

    Employee employee = employeeRepository.findById(id).orElseThrow(
        () -> new EmployeeNotFoundException(id)
    );
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

    long totalElements = employeeRepository.countEmployees(condition);

    log.info("직원 목록 조회 완료: count = {}", dtoList.size());

    return new CursorPageResponseEmployeeDto(
        dtoList, nextCursor, nextIdAfter, dtoList.size(),
        totalElements, hasNext
    );
  }


  @Override
  @Transactional
  public EmployeeDto update(Long id, EmployeeUpdateRequest request, MultipartFile profile, String ipAddress) {
    log.info("직원 정보 수정 요청: id = {}", id);

    Employee employee = employeeRepository.findById(id).orElseThrow(
        () -> new EmployeeNotFoundException(id)
    );

    if (!employee.getEmail().equals(request.email())
        && employeeRepository.existsByEmail(request.email())) {
      throw new EmailDuplicateException(request.email());
    }

    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));

    // =========================
    // ChangeLog 연동 작업 시작
    // 수정 전(before) 값 저장
    String beforeName = employee.getName();
    String beforeEmail = employee.getEmail();
    String beforePosition = employee.getPosition();
    Long beforeDepartmentId = employee.getDepartment().getId();
    LocalDate beforeHireDate = employee.getHireDate();
    EmployeeStatus beforeStatus = employee.getStatus();
    // =========================

    employee.update(request.name(),request.email(),
        department,request.position(),
        request.hireDate(),request.status());

    try {
      employeeRepository.flush();
    } catch (DataIntegrityViolationException e) {
      throw new EmailDuplicateException(request.email());
    }

    if (profile != null && !profile.isEmpty()) {
      File oldProfileImage = employee.getProfileImage();
      File newProfileImage = fileService.createFile(profile);
      employee.updateProfileImage(newProfileImage);

      if (oldProfileImage != null) {
        fileService.deleteFile(oldProfileImage.getId());
      }
    }

    // =========================
    // ChangeLog 연동 작업
    // 변경된 필드(before/after) 비교
    List<DiffDto> diffs = new ArrayList<>();

    if (!Objects.equals(beforeName, employee.getName())) {
      diffs.add(new DiffDto(
              "name",
              beforeName,
              employee.getName()
      ));
    }

    if (!Objects.equals(beforeEmail, employee.getEmail())) {
      diffs.add(new DiffDto(
              "email",
              beforeEmail,
              employee.getEmail()
      ));
    }

    if (!Objects.equals(beforePosition, employee.getPosition())) {
      diffs.add(new DiffDto(
              "position",
              beforePosition,
              employee.getPosition()
      ));
    }

    if (!Objects.equals(beforeDepartmentId, employee.getDepartment().getId())) {
      diffs.add(new DiffDto(
              "department",
              String.valueOf(beforeDepartmentId),
              String.valueOf(employee.getDepartment().getId())
      ));
    }

    if (!Objects.equals(beforeHireDate, employee.getHireDate())) {
      diffs.add(new DiffDto(
              "hireDate",
              beforeHireDate.toString(),
              employee.getHireDate().toString()
      ));
    }

    if (!Objects.equals(beforeStatus, employee.getStatus())) {
      diffs.add(new DiffDto(
              "status",
              beforeStatus.name(),
              employee.getStatus().name()
      ));
    } // =========================

    // =========================
    // ChangeLog 연동 작업
    // 직원 수정 이력 저장
    changeLogService.saveChangeLog(
            EmployeeChangeType.UPDATED,
            employee,
            employee.getEmployeeNumber(),
            request.memo(),
            ipAddress,
            diffs
    ); // =========================

    log.info("직원 정보 수정 완료: id = {}", id);
    return EmployeeDto.from(employee);
  }


  @Override
  @Transactional
  public void delete(Long id, String ipAddress) {
    log.info("직원정보 삭제 요청 id = {}", id);

    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new EmployeeNotFoundException(id));

    Long fileId = employee.getProfileImage() != null
        ? employee.getProfileImage().getId() : null;

    changeLogService.saveChangeLog(
        EmployeeChangeType.DELETED,
        employee,
        employee.getEmployeeNumber(),
        null,
        ipAddress,
        List.of()
    );
    employeeRepository.flush();   // ChangeLog INSERT 확정

    changeLogService.clearEmployeeReference(id);

    Employee target = employeeRepository.findById(id)
        .orElseThrow(() -> new EmployeeNotFoundException(id));

    employeeRepository.delete(target);
    employeeRepository.flush();

    // 4. 파일 삭제
    if (fileId != null) {
      fileService.deleteFile(fileId);
    }

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

  @Override
  @Transactional(readOnly = true)
  public long countEmployees(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
    log.info("직원 수 조회 요청: status = {}, fromDate = {}, toDate = {}", status, fromDate, toDate);

    long count = employeeRepository.countByCondition(status, fromDate, toDate);

    log.info("직원 수 조회 완료: count = {}", count);
    return count;
  }

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeDistributionDto> getDistribution(String groupBy, EmployeeStatus status) {
    log.info("직원 분포 조회 요청: groupBy = {}, status = {}", groupBy, status);

    String appliedGroupBy = (groupBy == null || groupBy.isBlank()) ? "department" : groupBy;
    EmployeeStatus appliedStatus = status != null ? status : EmployeeStatus.ACTIVE;

    return employeeRepository.countGroupByField(appliedGroupBy, appliedStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<EmployeeTrendDto> getTrend(LocalDate from, LocalDate to, String unit) {
    log.info("직원 수 추이 조회 요청: from = {}, to = {}, unit = {}", from, to, unit);

    String appliedUnit = (unit == null || unit.isBlank()) ? "month" : unit;
    LocalDate appliedTo = to != null ? to : LocalDate.now();
    LocalDate appliedFrom = from != null
        ? from : subtractUnits(appliedTo, appliedUnit, 11);
                           // to를 포함해 총 12구간이 나오도록, 시작점은 11구간 이전으로 잡음

    // 구간 끝 날짜들을 appliedFrom ~ appliedTo 범위에서, appliedTo를 기준으로 역산해 생성
    List<LocalDate> bucketEnds = new ArrayList<>();
    LocalDate cursor = appliedTo;
    while (!cursor.isBefore(appliedFrom)) {
      bucketEnds.add(0, cursor);
      cursor = stepBack(cursor, appliedUnit);
    }

    // 첫 구간의 변화율 계산을 위해, 범위 시작 이전 시점의 count도 미리 구함
    LocalDate beforeFirst = stepBack(bucketEnds.get(0), appliedUnit);
    long prevCount = employeeRepository.countHiredBefore(beforeFirst);

    List<EmployeeTrendDto> result = new ArrayList<>();
    for (LocalDate bucketEnd : bucketEnds) {
      long count = employeeRepository.countHiredBefore(bucketEnd);
      long change = count - prevCount;
      double changeRate = prevCount > 0
          ? Math.round((change * 10000.0 / prevCount)) / 100.0
          : 0.0;

      result.add(new EmployeeTrendDto(bucketEnd.toString(), count, change, changeRate));
      prevCount = count;
    }

    log.info("직원 수 추이 조회 완료: size = {}", result.size());
    return result;
  }

  // --- 헬퍼----

  // --- 헬퍼: unit에 따라 날짜를 한 구간만큼 뒤로 이동 ---
  private LocalDate stepBack(LocalDate date, String unit) {
    return switch (unit) {
      case "day" -> date.minusDays(1);
      case "week" -> date.minusWeeks(1);
      case "quarter" -> date.minusMonths(3);
      case "year" -> date.minusYears(1);
      default -> date.minusMonths(1); // month
    };
  }

  // --- 헬퍼: 기준일로부터 unit * count 만큼 이전 날짜 ---
  private LocalDate subtractUnits(LocalDate date, String unit, int count) {
    return switch (unit) {
      case "day" -> date.minusDays(count);
      case "week" -> date.minusWeeks(count);
      case "quarter" -> date.minusMonths(count * 3L);
      case "year" -> date.minusYears(count);
      default -> date.minusMonths(count); // month
    };
  }

}
