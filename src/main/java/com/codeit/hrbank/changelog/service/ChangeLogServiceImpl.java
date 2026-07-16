package com.codeit.hrbank.changelog.service;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.dto.*;
import com.codeit.hrbank.changelog.dto.request.ChangeLogSearchCondition;
import com.codeit.hrbank.changelog.dto.request.ChangeLogTypeFilter;
import com.codeit.hrbank.changelog.entity.EmployeeChangeDetail;
import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import com.codeit.hrbank.changelog.exception.ChangeLogNotFoundException;
import com.codeit.hrbank.changelog.exception.InvalidChangeLogSearchConditionException;
import com.codeit.hrbank.changelog.repository.EmployeeChangeDetailRepository;
import com.codeit.hrbank.changelog.repository.EmployeeChangeLogRepository;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeLogServiceImpl implements ChangeLogService {

    private static final ZoneId FRONTEND_TIME_ZONE =
            ZoneId.of("Asia/Seoul");

    private final EmployeeChangeLogRepository employeeChangeLogRepository;
    private final EmployeeChangeDetailRepository employeeChangeDetailRepository;

    // 수정 이력 목록 조회
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseChangeLogDto findChangeLogs(
            ChangeLogSearchCondition condition
    ) {
        int pageSize = condition.size() == null
                ? 10
                : condition.size();

        EmployeeChangeType appliedType =
                resolveTypeFilter(condition.type());

        if (pageSize < 1) {
            throw new InvalidChangeLogSearchConditionException(
                    "size는 1 이상이어야 합니다."
            );
        }

        String appliedSortField =
                resolveSortField(condition.sortField());

        String appliedSortDirection =
                resolveSortDirection(condition.sortDirection());
        /*
         * 목록 조회는 요청된 날짜 범위만 적용합니다.
         * 날짜가 없으면 기간 조건을 적용하지 않습니다.
         */
        Instant fromDate = condition.atFrom();
        Instant requestedToDate = condition.atTo();

        if (fromDate != null
                && requestedToDate != null
                && fromDate.isAfter(requestedToDate)) {
            throw new InvalidChangeLogSearchConditionException(
                    "atFrom은 atTo보다 이후일 수 없습니다."
            );
        }

        /*
         * 제공 프론트는 선택한 종료 날짜를 해당 날짜 00:00으로 전달합니다.
         * 종료 날짜 하루 전체가 포함되도록 다음 날 00:00 직전으로 보정합니다.
         */
        Instant toDate =
                resolveInclusiveEndDate(requestedToDate);

        /*
         * Repository 구현체에서는 pageSize + 1개를 조회합니다.
         * 추가 조회된 1개로 다음 페이지 존재 여부를 판단합니다.
         */
        List<EmployeeChangeLog> fetchedChangeLogs =
                employeeChangeLogRepository.findChangeLogs(
                        condition.employeeNumber(),
                        appliedType,
                        condition.memo(),
                        condition.ipAddress(),
                        fromDate,
                        toDate,
                        condition.idAfter(),
                        condition.cursor(),
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
                        condition.employeeNumber(),
                        appliedType,
                        condition.memo(),
                        condition.ipAddress(),
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
                .orElseThrow(() -> new ChangeLogNotFoundException(id));
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
        if (fromDate.isAfter(toDate)) {
            throw new InvalidChangeLogSearchConditionException(
                    "fromDate는 toDate보다 이후일 수 없습니다."
            );
        }
        return employeeChangeLogRepository.countByCreatedAtBetween(fromDate, toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeStatusChangeDto> findEmployeeStatusChanges() {
        return employeeChangeDetailRepository
                .findEmployeeStatusChangesForExistingEmployees()
                .stream()
                .map(detail -> {
                    EmployeeChangeLog changeLog = detail.getChangeLog();
                    Employee employee = changeLog.getEmployee();

                    return new EmployeeStatusChangeDto(
                            employee.getId(),
                            changeLog.getEmployeeNumber(),
                            EmployeeStatus.valueOf(detail.getBefore()),
                            EmployeeStatus.valueOf(detail.getAfter()),
                            changeLog.getCreatedAt()
                    );
                })
                .toList();
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

    @Override
    @Transactional
    public void clearEmployeeReference(Long employeeId) {
        employeeChangeLogRepository.clearEmployeeReference(employeeId);
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

        throw new InvalidChangeLogSearchConditionException(
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

        throw new InvalidChangeLogSearchConditionException(
                "sortDirection은 asc 또는 desc만 가능합니다."
        );
    }

    private EmployeeChangeType resolveTypeFilter(
            ChangeLogTypeFilter type
    ) {
        if (type == null || type == ChangeLogTypeFilter.ALL) {
            return null;
        }

        return EmployeeChangeType.valueOf(type.name());
    }

    private Instant resolveInclusiveEndDate(Instant atTo) {
        if (atTo == null) {
            return null;
        }

        boolean isStartOfDay = atTo
                .atZone(FRONTEND_TIME_ZONE)
                .toLocalTime()
                .equals(LocalTime.MIDNIGHT);

        if (!isStartOfDay) {
            return atTo;
        }

        return atTo
                .atZone(FRONTEND_TIME_ZONE)
                .plusDays(1)
                .minusNanos(1)
                .toInstant();
    }

}
