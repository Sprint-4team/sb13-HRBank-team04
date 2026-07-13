package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EmployeeChangeLogRepository extends JpaRepository<EmployeeChangeLog, Long> {

    long countByCreatedAtBetween(Instant fromDate, Instant toDate);

    @Query(
            """
            SELECT c
            FROM EmployeeChangeLog c
            WHERE (:employeeNumber IS NULL OR c.employeeNumber LIKE %:employeeNumber%)
            AND (:type IS NULL OR c.type = :type)
            AND (:memo IS NULL OR c.memo LIKE %:memo%)
            AND (:ipAddress IS NULL OR c.ipAddress LIKE %:ipAddress%)
            AND c.createdAt BETWEEN :fromDate AND :toDate
            ORDER BY c.createdAt DESC
            """
    )
    List<EmployeeChangeLog> findChangeLogs(
            @Param("employeeNumber") String employeeNumber,
            @Param("type") EmployeeChangeType type,
            @Param("memo") String memo,
            @Param("ipAddress") String ipAddress,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

}
