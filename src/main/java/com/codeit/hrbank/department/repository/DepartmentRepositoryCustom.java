package com.codeit.hrbank.department.repository;

import com.codeit.hrbank.department.dto.DepartmentSearchCondition;
import com.codeit.hrbank.department.entity.Department;
import java.util.List;

public interface DepartmentRepositoryCustom {
    List<Department> searchDepartments(DepartmentSearchCondition condition);
    long countDepartments(DepartmentSearchCondition condition);
}
