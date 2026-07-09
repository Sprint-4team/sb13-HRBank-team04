package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.entity.EmployeeChangeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeChangeDetailRepository extends JpaRepository<EmployeeChangeDetail, Long> {
}
