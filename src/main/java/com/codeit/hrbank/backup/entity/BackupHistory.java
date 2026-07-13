package com.codeit.hrbank.backup.entity;

import com.codeit.hrbank.backup.type.BackupStatus;
import com.codeit.hrbank.common.BaseEntity;
import com.codeit.hrbank.file.entity.File;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "backup_histories")
public class BackupHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String worker;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    private BackupHistory(String worker, BackupStatus status) {
        this.worker = worker;
        this.startedAt = Instant.now();
        this.status = status;
    }

    public static BackupHistory skipped(String worker) {
        BackupHistory history = new BackupHistory(worker, BackupStatus.SKIPPED);
        history.endedAt = Instant.now();

        return history;
    }

    public static BackupHistory inProgress(String worker) {
        return new BackupHistory(worker, BackupStatus.IN_PROGRESS);
    }

    public void complete(File file) {
        this.endedAt = Instant.now();
        this.status = BackupStatus.COMPLETED;
        this.file = file;
    }

    public void fail(File file) {
        this.endedAt = Instant.now();
        this.status = BackupStatus.FAILED;
        this.file = file;
    }

}
