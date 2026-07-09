package com.codeit.hrbank.backup.service;

import com.codeit.hrbank.backup.dto.request.BackupSearchCondition;
import com.codeit.hrbank.backup.dto.response.BackupDto;
import com.codeit.hrbank.backup.dto.response.CursorPageResponseBackupDto;
import com.codeit.hrbank.backup.entity.BackupHistory;
import com.codeit.hrbank.backup.repository.BackupHistoryRepository;
import com.codeit.hrbank.backup.type.BackupStatus;
import lombok.RequiredArgsConstructor;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackupHistoryService {

    private final BackupHistoryRepository backupHistoryRepository;

    @Transactional
    public BackupDto createBackupHistory(String worker) {

        //Todo 백업 필요 여부를 판단
        // 직원 레포지터리에서 특정 시간보다 updatedAt이 이후인 직원이 있거나 createdAt이 이후인 직원이 있을 시에 백업 inProgress로 진행
        // 백업이 필요 없다면 skipped로 진행

        //임시로 일단 그냥 백업 진행하도록
        BackupHistory backupHistory = BackupHistory.inProgress(worker);

        backupHistory = backupHistoryRepository.save(backupHistory);

        //Todo 백업 파일 저장 하는 로직


        return BackupDto.from(backupHistory);
    }

    @Transactional
    public CursorPageResponseBackupDto findBackupHistories(BackupSearchCondition condition) {


    }

    @Transactional
    public BackupDto findLatestBackupHistory(BackupStatus status) {
        BackupHistory latestBackupHistory = backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(status)
                //임시로 예외 발생하도록 구현함 -> 이후 명세대로 반환되도록 수정하기
                .orElseThrow(() -> new RuntimeException("에러: 해당 BackupHistory는 데이터파일에 존재하지 않습니다."));

        return BackupDto.from(latestBackupHistory);
    }

}
