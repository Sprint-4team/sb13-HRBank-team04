package com.codeit.hrbank.backup.service;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.repository.BackupHistoryRepository;
import com.codeit.hrbank.backup.type.BackupStatus;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import com.codeit.hrbank.changelog.repository.EmployeeChangeLogRepository;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BackupHistoryService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final BackupHistoryRepository backupHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeChangeLogRepository employeeChangeLogRepository;
    private final BackupHistoryCreationService backupHistoryCreationService;

    //저장 디렉토리
    @Value("${file.upload-dir}")
    private Path uploadDir;

    // CSV 파일의 컬럼 순서. 헤더 행과 데이터 행 모두 이 순서를 따른다.
    private static final String CSV_HEADER =
            "ID,직원번호,이름,이메일,부서,직급,입사일,상태";

    @Transactional
    public BackupDto createBackupHistory(String worker) {
        // status가 COMPLETED인 가장 최근 백업 정보 검색
        Optional<BackupHistory> lastBackupHistory = backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED);

        // 최근 완료 백업이 있으면 그 이후 직원 변경 여부 확인
        // 완료 백업이 없으면 최초 백업이므로 true
        boolean isBackupNeed = lastBackupHistory
                .map(backupHistory ->
                        employeeChangeLogRepository.existsByCreatedAtAfter(backupHistory.getStartedAt()))
                .orElse(true);

        // 백업 필요 시 IN_PROGRESS, 필요 없으면 SKIPPED 이력 생성
        // 별도 트랜잭션 생성되도록 구현함 -> Propagation.REQUIRES_NEW
        Long backupHistoryId = (isBackupNeed)
                ? backupHistoryCreationService.createInProgress(worker)
                : backupHistoryCreationService.createSkipped(worker);

        // 생성된 백업 이력 조회
        BackupHistory backupHistory = backupHistoryRepository.findById(backupHistoryId)
                .orElseThrow(() -> new RuntimeException("방금 막 생성한 백업 정보가 없습니다."));

        // 백업 skip 시 프로세스 종료
        if (!isBackupNeed)
            return BackupDto.from(backupHistory);

        // 백업 파일 저장 하는 로직 시작
        // 백업 파일 경로 지정
        String fileName = DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmmss")
                .withZone(ZoneId.systemDefault())
                .format(backupHistory.getStartedAt()) + ".csv";
        Path backupFilePath = uploadDir.resolve("backups").resolve(fileName);

        // 백업 파일 저장
        try {
            saveToCSV(backupFilePath);
        } catch (IOException e) {
            // 백업 파일 저장 실패 시
//            backupHistory.fail();   //로그 파일 파라미터로 추가
            //예외처리하기
//            throw new RuntimeException(e);
        }

        // 백업 파일 저장 성공 시
//        backupHistory.complete();  //파일 관련 PR 머지되면 파일 객체도 파라미터로 추가

        return BackupDto.from(backupHistory);
    }

    // 백업 이력 목록을 조회, 커서 페이지 응답 DTO반환
    @Transactional(readOnly = true)
    public CursorPageResponseBackupDto findBackupHistories(BackupSearchCondition condition) {
        int pageSize = normalizePageSize(condition.size());
        Instant cursorStartedAt = parseCursorStartedAt(condition.cursor());

        //size+1 건 조회, 1건 더 있을 시 hasnext true
        List<BackupHistory> fetchedHistories = findPageHistories(condition, cursorStartedAt, pageSize);

        boolean hasNext = fetchedHistories.size() > pageSize;
        List<BackupHistory> pageHistories = hasNext
                ? fetchedHistories.subList(0, pageSize)
                : fetchedHistories;

        List<BackupDto> content = pageHistories.stream()
                .map(BackupDto::from)
                .toList();

        long totalElements = backupHistoryRepository.countHistories(
                condition.worker(),
                condition.status(),
                condition.startedAtFrom(),
                condition.startedAtTo()
        );

        if (pageHistories.isEmpty()) {
            return new CursorPageResponseBackupDto(content, null, null, pageSize, totalElements, false);
        }

        BackupHistory lastHistory = pageHistories.get(pageHistories.size() - 1);
        return new CursorPageResponseBackupDto(
                content,
                lastHistory.getStartedAt().toString(),
                lastHistory.getId(),
                pageSize,
                totalElements,
                hasNext
        );
    }

    // 최신 1건 조회
    @Transactional(readOnly = true)
    public BackupDto findLatestBackupHistory(BackupStatus status) {
        BackupHistory latestBackupHistory = backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(status)
                .orElseThrow(() -> new RuntimeException("백업 히스토리 없음"));

        return BackupDto.from(latestBackupHistory);
    }


    private List<BackupHistory> findPageHistories(
            BackupSearchCondition condition,
            Instant cursorStartedAt,
            int pageSize
    ) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);

        if (cursorStartedAt == null || condition.idAfter() == null) {
            return backupHistoryRepository.findHistories(
                    condition.worker(),
                    condition.status(),
                    condition.startedAtFrom(),
                    condition.startedAtTo(),
                    pageRequest
            );
        }

        return backupHistoryRepository.findHistoriesAfter(
                condition.worker(),
                condition.status(),
                condition.startedAtFrom(),
                condition.startedAtTo(),
                cursorStartedAt,
                condition.idAfter(),
                pageRequest
        );
    }

    // size null일 시 처리, 너무 큰 값은 최대값으로 제한
    private int normalizePageSize(Integer requestedSize) {
        if (requestedSize == null || requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(requestedSize, MAX_PAGE_SIZE);
    }

    // cursor는 마지막으로 조회한 백업 이력의 startedAt 값.
    private Instant parseCursorStartedAt(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(cursor);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("커서 : " + cursor, e);
        }
    }

    // CSV로 저장 로직
    private void saveToCSV(Path backupFilePath) throws IOException {
        Path parent = backupFilePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(backupFilePath, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.newLine();

            int page = 0;
            int size = 30;
            while (true) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
                Page<Employee> employeePage = employeeRepository.findAll(pageable);

                for (Employee employee : employeePage.getContent()) {
                    writer.write(toCsvRow(employee));
                    writer.newLine();
                }

                if (!employeePage.hasNext()) {
                    break;
                }

                page++;
            }
        }

    }

    // 직원을 CSV 한 행으로 직렬화
    private String toCsvRow(Employee employee) {
        return String.join(",",
                String.valueOf(employee.getId()),
                employee.getEmployeeNumber(),
                employee.getName(),
                employee.getEmail(),
                String.valueOf(employee.getDepartmentId()), // Todo employee.getDepartment().getName(), 나중에 employee 저장할 때 id 말고 employee 객체를 저장하도록 요청하기
                employee.getPosition(),
                String.valueOf(employee.getHireDate()),
                String.valueOf(employee.getStatus())
        );
    }

}
