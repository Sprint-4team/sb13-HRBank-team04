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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "직원 관리", description = "직원 등록, 수정, 삭제, 조회 API")
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @Operation(summary = "직원 등록", description = "이름, 이메일, 부서, 직함, 입사일, 프로필 이미지를 입력해 새로운 직원을 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "등록 성공"),
      @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음"),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
  })
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

  @Operation(summary = "직원 상세 조회", description = "직원 ID로 직원의 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "해당 ID의 직원이 존재하지 않음")
  })
  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> findEmployee(
      @Parameter(description = "조회할 직원의 ID", example = "1")
      @PathVariable Long id){
    EmployeeDto employee = employeeService.findEmployee(id);
    return ResponseEntity.status(HttpStatus.OK).body(employee);
  }

  @Operation(summary = "직원 목록 조회", description = "이름/이메일, 부서, 직함, 사원번호, 입사일, 상태로 직원 목록을 검색하고, 이름/입사일/사원번호 기준으로 정렬 및 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseEmployeeDto> findEmployees(
      @Parameter(description = "이름 또는 이메일 검색어 (부분 일치)")
      @RequestParam(required = false) String nameOrEmail,
      @Parameter(description = "부서명 검색어 (부분 일치)")
      @RequestParam(required = false) String departmentName,
      @Parameter(description = "직함 검색어 (부분 일치)")
      @RequestParam(required = false) String position,
      @Parameter(description = "사원번호 검색어 (부분 일치)")
      @RequestParam(required = false) String employeeNumber,
      @Parameter(description = "입사일 범위 시작")
      @RequestParam(required = false) LocalDate hireDateFrom,
      @Parameter(description = "입사일 범위 종료")
      @RequestParam(required = false) LocalDate hireDateTo,
      @Parameter(description = "직원 상태 (완전 일치)")
      @RequestParam(required = false) EmployeeStatus status,
      @Parameter(description = "커서 (다음 페이지 시작점)")
      @RequestParam(required = false) String cursor,
      @Parameter(description = "이전 페이지 마지막 요소 ID")
      @RequestParam(required = false) Long idAfter,
      @Parameter(description = "페이지 크기", example = "10")
      @RequestParam(defaultValue = "10") int size,
      @Parameter(description = "정렬 필드 (name, hireDate, employeeNumber)", example = "name")
      @RequestParam(defaultValue = "name") String sortField,
      @Parameter(description = "정렬 방향 (ASC 또는 DESC)", example = "ASC")
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    EmployeeSearchCondition condition = new EmployeeSearchCondition(
        nameOrEmail, departmentName, position, employeeNumber,
        hireDateFrom, hireDateTo, status,
        cursor, idAfter, size, sortField,
        Sort.Direction.fromString(sortDirection)
    );

    return ResponseEntity.ok(employeeService.findEmployees(condition));
  }

  @Operation(summary = "직원 수 조회", description = "지정된 조건에 맞는 직원 수를 조회합니다. 상태 필터링 및 입사일 기간 필터링이 가능합니다.")
  @ApiResponses({ @ApiResponse(responseCode = "200", description = "조회 성공")})
  @GetMapping("/count")
  public ResponseEntity<Long> countEmployees(
      @Parameter(description = "직원 상태 (재직중, 휴직중, 퇴사)")
      @RequestParam(required = false) EmployeeStatus status,
      @Parameter(description = "입사일 시작 (지정 시 해당 기간 내 입사한 직원 수 조회)")
      @RequestParam(required = false) LocalDate fromDate,
      @Parameter(description = "입사일 종료 (fromDate와 함께 사용, 기본값: 현재 일시)")
      @RequestParam(required = false) LocalDate toDate
  ) {
    long count = employeeService.countEmployees(status, fromDate, toDate);
    return ResponseEntity.status(HttpStatus.OK).body(count);
  }


  @Operation(summary = "직원 분포 조회", description = "지정된 기준으로 그룹화된 직원 분포를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공")
  })
  @GetMapping("/stats/distribution")
  public ResponseEntity<List<EmployeeDistributionDto>> getDistribution(
      @Parameter(description = "그룹화 기준 (department: 부서별, position: 직무별, 기본값: department)", example = "department")
      @RequestParam(defaultValue = "department") String groupBy,
      @Parameter(description = "직원 상태 (재직중, 휴직중, 퇴사, 기본값: 재직중)", example = "ACTIVE")
      @RequestParam(required = false) EmployeeStatus status
  ) {
    List<EmployeeDistributionDto> distribution = employeeService.getDistribution(groupBy, status);
    return ResponseEntity.status(HttpStatus.OK).body(distribution);
  }

  @Operation(summary = "직원 수 추이 조회", description = "지정된 기간 및 시간 단위로 그룹화된 직원 수 추이를 조회합니다. 파라미터를 제공하지 않으면 최근 12개월 데이터를 월 단위로 반환합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공")
  })
  @GetMapping("/stats/trend")
  public ResponseEntity<List<EmployeeTrendDto>> getTrend(
      @Parameter(description = "시작 일시 (기본값: 현재로부터 unit 기준 12개 이전)")
      @RequestParam(required = false) LocalDate from,
      @Parameter(description = "종료 일시 (기본값: 현재)")
      @RequestParam(required = false) LocalDate to,
      @Parameter(description = "시간 단위 (day, week, month, quarter, year, 기본값: month)", example = "month")
      @RequestParam(defaultValue = "month") String unit
  ){
    List<EmployeeTrendDto> trend = employeeService.getTrend(from, to, unit);
    return ResponseEntity.status(HttpStatus.OK).body(trend);
  }


  @Operation(summary = "직원 정보 수정", description = "사원번호를 제외한 직원의 모든 속성을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음"),
      @ApiResponse(responseCode = "404", description = "해당 ID의 직원이 존재하지 않음"),
      @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일")
  })
  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EmployeeDto> update(
      @Parameter(description = "수정할 직원의 ID", example = "1")
      @PathVariable Long id,
      @RequestPart("employee") @Valid EmployeeUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile,
      HttpServletRequest httpServletRequest
  ) {
    String ipAddress = CommonUtils.getRemoteIp(httpServletRequest);
    EmployeeDto updated = employeeService.update(id, request, profile, ipAddress);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "직원 삭제", description = "직원을 삭제합니다. 프로필 이미지가 있는 경우 함께 삭제됩니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "404", description = "해당 ID의 직원이 존재하지 않음")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 직원의 ID", example = "1")
      @PathVariable Long id, HttpServletRequest httpServletRequest){
    String ipAddress = CommonUtils.getRemoteIp(httpServletRequest);
    employeeService.delete(id, ipAddress);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
