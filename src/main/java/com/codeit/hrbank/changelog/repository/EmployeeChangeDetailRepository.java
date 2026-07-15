package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.entity.EmployeeChangeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeChangeDetailRepository extends JpaRepository<EmployeeChangeDetail, Long> {

    List<EmployeeChangeDetail> findByChangeLogId(Long changeLogId);

    @Query("""
            SELECT detail
            FROM EmployeeChangeDetail detail
            JOIN FETCH detail.changeLog changeLog
            JOIN FETCH changeLog.employee employee
            WHERE detail.propertyName = 'status'
            ORDER BY employee.id ASC,
                     changeLog.createdAt ASC,
                     changeLog.id ASC,
                     detail.id ASC
            """)
    List<EmployeeChangeDetail> findEmployeeStatusChangesForExistingEmployees();

}
