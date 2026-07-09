package com.codeit.hrbank.backup.entity;

import com.codeit.hrbank.backup.type.BackupStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Table(name = "backup_histories")
public class BackupHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(nullable = false)
    private String worker;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupStatus status;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "file_id")
//    private 파일엔티티 file;

    private BackupHistory(String worker, BackupStatus status) {
        Instant now = Instant.now();

        this.worker = worker;
        this.startedAt = now;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public static BackupHistory skipped(String worker) {
        BackupHistory history = new BackupHistory(worker, BackupStatus.SKIPPED);
        history.endedAt = Instant.now();

        return history;
    }

    public static BackupHistory inProgress(String worker) {
        return new BackupHistory(worker, BackupStatus.IN_PROGRESS);
    }

    public void complete(Instant endedAt) {
        this.updatedAt = Instant.now();
        this.endedAt = endedAt;
        this.status = BackupStatus.COMPLETED;
//        this.file = file;
    }

    public void fail(Instant endedAt) {
        this.updatedAt = Instant.now();
        this.endedAt = endedAt;
        this.status = BackupStatus.FAILED;
//        this.file = file;
    }

}
