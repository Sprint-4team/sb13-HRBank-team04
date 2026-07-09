package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeChangeLogRepository extends JpaRepository<EmployeeChangeLog, Long> {
}
