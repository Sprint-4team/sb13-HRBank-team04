package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EmployeeChangeLogRepository extends JpaRepository<EmployeeChangeLog, Long>, EmployeeChangeLogRepositoryCustom {

    long countByCreatedAtBetween(Instant fromDate, Instant toDate);

    boolean existsByCreatedAtAfter(Instant createdAt);

}
