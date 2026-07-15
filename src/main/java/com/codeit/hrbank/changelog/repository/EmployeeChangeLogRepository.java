package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface EmployeeChangeLogRepository extends JpaRepository<EmployeeChangeLog, Long>, EmployeeChangeLogRepositoryCustom {

    long countByCreatedAtBetween(Instant fromDate, Instant toDate);

    boolean existsByCreatedAtAfter(Instant createdAt);

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
        UPDATE EmployeeChangeLog c
        SET c.employee = null
        WHERE c.employee.id = :employeeId
        """)
    int clearEmployeeReference(
            @Param("employeeId") Long employeeId
    );
}
