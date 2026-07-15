package com.codeit.hrbank.backup.service;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.exception.BackupHistoryNotFoundException;
import com.codeit.hrbank.backup.repository.BackupHistoryRepository;
import com.codeit.hrbank.backup.type.BackupStatus;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.codeit.hrbank.changelog.repository.EmployeeChangeLogRepository;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import com.codeit.hrbank.file.entity.File;
import com.codeit.hrbank.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupHistoryService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final BackupHistoryRepository backupHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeChangeLogRepository employeeChangeLogRepository;
    private final FileRepository fileRepository;
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
                .orElseThrow(() -> new BackupHistoryNotFoundException(backupHistoryId));

        // 백업 skip 시 프로세스 종료
        if (!isBackupNeed)
            return BackupDto.from(backupHistory);

        // 백업 파일 경로 지정
        String fileName = createFileName(backupHistory.getStartedAt(), ".csv");
        Path backupFilePath = uploadDir.resolve("backups").resolve(fileName);

        // 백업 파일 저장
        try {
            //CSV 파일 저장
            saveToCSV(backupFilePath);

            //파일 객체 생성 및 저장
            File backupFile = createFileEntity(fileName, "text/csv", backupFilePath);

            // 백업 이력 완료로 수정
            backupHistory.complete(backupFile);

        } catch (IOException e) {
            // 저장하던 파일 삭제
            try {
                deleteIfExists(backupFilePath);
            } catch (IOException deleteException) {
                // 여기서 발생한 예외를 기존 예외에 추가
                e.addSuppressed(deleteException);
            }

            // 에러 로그 파일 경로 지정
            String errorLogFileName = createFileName(backupHistory.getStartedAt(), ".log");
            Path errorLogFilePath = uploadDir.resolve("backups").resolve("logs").resolve(errorLogFileName);

            // 파일 저장
            try {
                //에러 로그 파일 저장
                saveErrorLog(errorLogFilePath, e);

                //파일 객체 생성 및 저장
                File logFile = createFileEntity(errorLogFileName, "text/plain", errorLogFilePath);

                // 백업 이력 실패로 수정
                backupHistory.fail(logFile);

            } catch (IOException exception) {
                //에러 로그 파일을 저장하려는 것도 실패
                backupHistory.fail();

                // 여기서 발생한 예외를 기존 예외에 추가
                exception.addSuppressed(e);

                //최후의 최후에는 예외 던지면 IN_PROGRESS 상태 백업 기록만 남고 실패 상태가 트랜잭션 롤백되므로, 로그를 남겨서 트랜잭션 커밋시키도록 구현
                log.error("백업 작업 실패 후 에러 로그 파일 저장에도 실패했습니다. backupHistoryId: {}", backupHistory.getId(), exception);
            }
        }

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
                employee.getDepartment().getName(),
                employee.getPosition(),
                String.valueOf(employee.getHireDate()),
                String.valueOf(employee.getStatus())
        );
    }

    //파일 이름 생성 메서드
    private String createFileName(Instant startedAt, String type) {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmmss")
                .withZone(ZoneId.systemDefault())
                .format(startedAt) + type;
    }
    //파일 객체 생성 및 저장하는 메서드
    private File createFileEntity(String fileName, String contentType, Path filePath) throws IOException {
        File file = new File(
                fileName,
                fileName,
                contentType,
                Files.size(filePath),
                filePath.toAbsolutePath().normalize().toString()
        );

        return fileRepository.save(file);
    }
    // 해당 경로에 파일이 존재하면 삭제하는 메서드
    private void deleteIfExists(Path filePath) throws IOException {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IOException("저장 중이던 백업 파일 삭제에 실패했습니다. filePath: " + filePath, e);
        }
    }
    // 에러 로그 저장 메서드
    private void saveErrorLog(Path errorLogFilePath, Exception exception) throws IOException {
        //상위 디렉터리 없을 경우 생성
        Path parent = errorLogFilePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // 로그 작성
        String logContent = """
                백업 실패 시간: %s
                예외 타입: %s
                예외 메시지: %s
                """.formatted(
                Instant.now(),
                exception.getClass().getName(),
                exception.getMessage()
        );

        //파일 작성
        Files.writeString(errorLogFilePath, logContent, StandardCharsets.UTF_8);
    }

}
