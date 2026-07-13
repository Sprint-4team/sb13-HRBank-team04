package com.codeit.hrbank.backup.service;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.repository.BackupHistoryRepository;
import com.codeit.hrbank.backup.type.BackupStatus;
import com.codeit.hrbank.changelog.repository.EmployeeChangeLogRepository;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class BackupHistoryService {

    private final BackupHistoryRepository backupHistoryRepository;
    private final EmployeeRepository employeeRepository;
    // Todo EmployeeChangeLog 관련 PR 머지되면 주석 풀기
    private final EmployeeChangeLogRepository employeeChangeLogRepository;

    //저장 디렉토리
    @Value("${file.upload-dir}")
    private Path uploadDir;

    // CSV 파일의 컬럼 순서. 헤더 행과 데이터 행 모두 이 순서를 따른다.
    private static final String CSV_HEADER =
            "ID,직원번호,이름,이메일,부서,직급,입사일,상태";

    @Transactional
    public BackupDto createBackupHistory(String worker) {

        // status가 COMPLETED인 가장 최근 백업 정보 검색
        BackupHistory lastBackupHistory = backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED)
                .orElseThrow(() -> new RuntimeException("dff"));

        // Todo EmployeeChangeLog 관련 PR 머지되면 주석 풀기
        // 가장 최근 완료된 배치 작업 시간 이후 직원 데이터가 변경된 경우 있는지 체크
        boolean isBackupNeed = employeeChangeLogRepository.existsByCreatedAtAfter(lastBackupHistory.getStartedAt());

        // 백업 필요 시 inProgress 상태로 백업 시작, 백업 필요 없을 시 skipped 상태로 백업 건너뛰기
        BackupHistory backupHistory = (isBackupNeed)
                ? BackupHistory.inProgress(worker)
                : BackupHistory.skipped(worker);

        // 백업 이력 저장
        backupHistory = backupHistoryRepository.save(backupHistory);

        // 백업 skip 시 프로세스 종료
        if (!isBackupNeed)
            return BackupDto.from(backupHistory);

        // 백업 파일 저장 하는 로직
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
            backupHistory.fail(Instant.now());
            //예외처리하기
//            throw new RuntimeException(e);
        }

        // Todo 파일 저장 로직 (파일 관련 PR 머지되면 진행)

        // 백업 파일 저장 성공 시
        backupHistory.complete(Instant.now());  //파일 관련 PR 머지되면 파일 객체도 파라미터로 추가

        return BackupDto.from(backupHistory);
    }

    @Transactional
    public void findBackupHistories(BackupSearchCondition condition) {


    }

    @Transactional
    public BackupDto findLatestBackupHistory(BackupStatus status) {
        BackupHistory latestBackupHistory = backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(status)
                //임시로 예외 발생하도록 구현함 -> 이후 명세대로 반환되도록 수정하기
                .orElseThrow(() -> new RuntimeException("에러: 해당 BackupHistory는 데이터파일에 존재하지 않습니다."));

        return BackupDto.from(latestBackupHistory);
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
                String.valueOf(employee.getDepartmentId()), //employee.getDepartment().getName(), 나중에 employee 저장할 때 id 말고 employee 객체를 저장하도록 요청하기
                employee.getPosition(),
                String.valueOf(employee.getHireDate()),
                String.valueOf(employee.getStatus())
        );
    }

}
