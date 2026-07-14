package com.codeit.hrbank.department.service;

import com.codeit.hrbank.department.dto.*;

public interface DepartmentService {

    DepartmentDto create(DepartmentCreateRequest request);

    DepartmentDto update(Long id, DepartmentUpdateRequest request);

    DepartmentDto find(Long id);

    void delete(Long id);

    CursorPageResponseDepartmentDto findAll(DepartmentSearchCondition condition);
}