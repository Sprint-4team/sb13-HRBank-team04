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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeLogServiceImpl implements ChangeLogService{

    private final EmployeeChangeLogRepository employeeChangeLogRepository;
    private final EmployeeChangeDetailRepository employeeChangeDetailRepository;

    // 수정 이력 목록 조회
    // TODO: Repository에서 cursor, idAfter, size, sortField, sortDirection을 지원하도록 수정 후 커서 페이지네이션 적용
    @Override
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
        // 1. 기본값 처리
        // atFrom이 없으면 7일 전
        // atTo가 없으면 현재 시각
        Instant fromDate = atFrom != null
                ? atFrom
                : Instant.now().minus(7, ChronoUnit.DAYS);
        Instant to = atTo != null
                ? atTo
                : Instant.now();

        // 2. Repository 조회
        List<EmployeeChangeLog> changeLogs =
                employeeChangeLogRepository.findChangeLogs(
                        employeeNumber,
                        type,
                        memo,
                        ipAddress,
                        fromDate,
                        to
                );

        // 3. ChangeLogDto 변환
        List<ChangeLogDto> content = changeLogs.stream()
                .map(changeLog -> new ChangeLogDto(
                        changeLog.getId(),
                        changeLog.getType(),
                        changeLog.getEmployeeNumber(),
                        changeLog.getMemo(),
                        changeLog.getIpAddress(),
                        changeLog.getCreatedAt()
                ))
                .toList();

        // TODO: Repository에서 cursor 기반 페이지네이션 지원 후 적용
        List<EmployeeChangeLog> contentLogs = changeLogs.stream()
                .limit(size)
                .toList();

        // 4. nextCursor 생성
        String nextCursor = content.isEmpty()
                ? null
                : content.get(content.size() - 1).at().toString();

        // 5. nextIdAfter 생성
        Long nextIdAfter = content.isEmpty()
                ? null
                : content.get(content.size() - 1).id();

        // 6. totalElements
        Long totalElements =
                employeeChangeLogRepository.countByCreatedAtBetween(fromDate, to);

        // 7. hasNext 계산
        boolean hasNext = changeLogs.size() > size;

        // 8. 반환
        return new CursorPageResponseChangeLogDto(
                content,
                nextCursor,
                nextIdAfter,
                size,
                totalElements,
                hasNext
        );
    }

    // 수정 이력 상세 조회
    @Override
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
        return new ChangeLogDetailDto(
                changeLog.getId(),
                changeLog.getType(),
                changeLog.getEmployeeNumber(),
                changeLog.getMemo(),
                changeLog.getIpAddress(),
                changeLog.getCreatedAt(),
                null, // TODO Employee 연동 후 employeeName 반환
                null, // TODO Employee 연동 후 profileImageId 반환
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
    public void saveChangeLog(
            EmployeeChangeType type,
            String employeeNumber,
            String memo,
            String ipAddress
    ) {
        // TODO: 직원 생성/수정/삭제 시 호출
        // EmployeeChangeLog 저장
        // EmployeeChangeDetail 저장
    }

}
