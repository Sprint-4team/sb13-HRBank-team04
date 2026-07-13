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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackupHistoryService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final BackupHistoryRepository backupHistoryRepository;

    @Transactional
    public BackupDto createBackupHistory(String worker) {
        BackupHistory backupHistory = BackupHistory.inProgress(worker);
        BackupHistory savedBackupHistory = backupHistoryRepository.save(backupHistory);

        return BackupDto.from(savedBackupHistory);
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
}
