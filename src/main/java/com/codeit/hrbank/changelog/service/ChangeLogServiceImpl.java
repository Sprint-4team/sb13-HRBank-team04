package com.codeit.hrbank.changelog.service;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.dto.ChangeLogDetailDto;
import com.codeit.hrbank.changelog.dto.ChangeLogDto;
import com.codeit.hrbank.changelog.dto.CursorPageResponseChangeLogDto;
import com.codeit.hrbank.changelog.dto.DiffDto;
import com.codeit.hrbank.changelog.entity.EmployeeChangeDetail;
import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import com.codeit.hrbank.changelog.repository.EmployeeChangeDetailRepository;
import com.codeit.hrbank.changelog.repository.EmployeeChangeLogRepository;
import com.codeit.hrbank.employee.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeLogServiceImpl implements ChangeLogService{

    private final EmployeeChangeLogRepository employeeChangeLogRepository;
    private final EmployeeChangeDetailRepository employeeChangeDetailRepository;

    // 수정 이력 목록 조회
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseChangeLogDto findChangeLogs(
            String employeeNumber,
            EmployeeChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo,
            Long idAfter,
            String cursor,
            Integer size,
            String sortField,
            String sortDirection
    ) {
        int pageSize = size == null ? 10 : size;

        if (pageSize < 1) {
            throw new IllegalArgumentException(
                    "size는 1 이상이어야 합니다."
            );
        }

        String appliedSortField = resolveSortField(sortField);
        String appliedSortDirection =
                resolveSortDirection(sortDirection);

        /*
         * 종료 시각을 먼저 확정하고,
         * 시작 시각이 없으면 종료 시각 기준 7일 전으로 설정합니다.
         */
        Instant toDate = atTo == null
                ? Instant.now()
                : atTo;

        Instant fromDate = atFrom == null
                ? toDate.minus(7, ChronoUnit.DAYS)
                : atFrom;

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "atFrom은 atTo보다 이후일 수 없습니다."
            );
        }

        /*
         * Repository 구현체에서는 pageSize + 1개를 조회합니다.
         * 추가 조회된 1개로 다음 페이지 존재 여부를 판단합니다.
         */
        List<EmployeeChangeLog> fetchedChangeLogs =
                employeeChangeLogRepository.findChangeLogs(
                        employeeNumber,
                        type,
                        memo,
                        ipAddress,
                        fromDate,
                        toDate,
                        idAfter,
                        cursor,
                        pageSize,
                        appliedSortField,
                        appliedSortDirection
                );

        boolean hasNext =
                fetchedChangeLogs.size() > pageSize;

        /*
         * Repository가 size + 1개를 반환한 경우,
         * 실제 응답에는 요청한 size만큼만 포함합니다.
         */
        List<EmployeeChangeLog> pageChangeLogs = hasNext
                ? fetchedChangeLogs.subList(0, pageSize)
                : fetchedChangeLogs;

        List<ChangeLogDto> content = pageChangeLogs.stream()
                .map(changeLog -> new ChangeLogDto(
                        changeLog.getId(),
                        changeLog.getType(),
                        changeLog.getEmployeeNumber(),
                        changeLog.getMemo(),
                        changeLog.getIpAddress(),
                        changeLog.getCreatedAt()
                ))
                .toList();

        String nextCursor = null;
        Long nextIdAfter = null;

        /*
         * 다음 페이지가 있는 경우에만
         * 현재 페이지 마지막 요소의 정렬값과 ID를 반환합니다.
         */
        if (hasNext && !pageChangeLogs.isEmpty()) {
            EmployeeChangeLog lastChangeLog =
                    pageChangeLogs.get(pageChangeLogs.size() - 1);

            nextIdAfter = lastChangeLog.getId();

            if ("ipAddress".equals(appliedSortField)) {
                nextCursor = lastChangeLog.getIpAddress();
            } else {
                nextCursor =
                        lastChangeLog.getCreatedAt().toString();
            }
        }

        long totalElements =
                employeeChangeLogRepository.countChangeLogs(
                        employeeNumber,
                        type,
                        memo,
                        ipAddress,
                        fromDate,
                        toDate
                );

        return new CursorPageResponseChangeLogDto(
                content,
                nextCursor,
                nextIdAfter,
                pageSize,
                totalElements,
                hasNext
        );
    }

    // 수정 이력 상세 조회
    @Override
    @Transactional(readOnly = true)
    public ChangeLogDetailDto findChangeLogDetail(Long id) {
        EmployeeChangeLog changeLog = employeeChangeLogRepository.findById(id)
                .orElseThrow(); // 예외 클래스로 추후 수정
        List<EmployeeChangeDetail> details =
                employeeChangeDetailRepository.findByChangeLogId(id);
        List<DiffDto> diffs = details.stream()
                .map(detail -> new DiffDto(
                        detail.getPropertyName(),
                        detail.getBefore(),
                        detail.getAfter()
                ))
                .toList();
        Employee employee = changeLog.getEmployee();
        String employeeName = employee != null
                ? employee.getName()
                : null;
        Long profileImageId =
                employee != null && employee.getProfileImage() != null
                        ? employee.getProfileImage().getId()
                        : null;
        return new ChangeLogDetailDto(
                changeLog.getId(),
                changeLog.getType(),
                changeLog.getEmployeeNumber(),
                changeLog.getMemo(),
                changeLog.getIpAddress(),
                changeLog.getCreatedAt(),
                employeeName,
                profileImageId,
                diffs
        );
    }

    // 수정 이력 건수 조회 (기본값: 최근 7일)
    @Override
    public Long findChangeLogDetailCount(
            Instant fromDate,
            Instant toDate
    ) {
        // 조회 기간이 지정되지 않으면 최근 7일을 기본 조회 기간으로 사용
        if (toDate == null) {
            toDate = Instant.now();
        }
        if (fromDate == null) {
            fromDate = toDate.minus(7, ChronoUnit.DAYS);
        }
        return employeeChangeLogRepository.countByCreatedAtBetween(fromDate, toDate);
    }

    @Override
    @Transactional
    public void saveChangeLog(
            EmployeeChangeType type,
            Employee employee,
            String employeeNumber,
            String memo,
            String ipAddress,
            List<DiffDto> diffs
    ) {
        // TODO: 직원 생성/수정/삭제 시 호출
        // EmployeeChangeLog 저장
        // EmployeeChangeDetail 저장
        EmployeeChangeLog changeLog = EmployeeChangeLog.builder()
                .type(type)
                .employee(employee)
                .employeeNumber(employeeNumber)
                .memo(memo)
                .ipAddress(ipAddress)
                .build();

        EmployeeChangeLog savedChangeLog = employeeChangeLogRepository.save(changeLog);

        for (DiffDto diff : diffs) {
            EmployeeChangeDetail detail = EmployeeChangeDetail.builder()
                    .changeLog(savedChangeLog)
                    .propertyName(diff.propertyName())
                    .before(diff.before())
                    .after(diff.after())
                    .build();

            employeeChangeDetailRepository.save(detail);
        }
    }

    private String resolveSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "at";
        }

        if ("at".equalsIgnoreCase(sortField)) {
            return "at";
        }

        if ("ipAddress".equalsIgnoreCase(sortField)) {
            return "ipAddress";
        }

        throw new IllegalArgumentException(
                "sortField는 at 또는 ipAddress만 가능합니다."
        );
    }

    private String resolveSortDirection(
            String sortDirection
    ) {
        if (sortDirection == null
                || sortDirection.isBlank()) {
            return "desc";
        }

        if ("asc".equalsIgnoreCase(sortDirection)) {
            return "asc";
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            return "desc";
        }

        throw new IllegalArgumentException(
                "sortDirection은 asc 또는 desc만 가능합니다."
        );
    }

    @Override
    @Transactional
    public void clearEmployeeReference(Long employeeId) {
        employeeChangeLogRepository.clearEmployeeReference(employeeId);
    }

}
