package com.codeit.hrbank.department.service;

import com.codeit.hrbank.department.dto.DepartmentCreateRequest;
import com.codeit.hrbank.department.dto.DepartmentDto;
import com.codeit.hrbank.department.dto.DepartmentUpdateRequest;

public interface DepartmentService {

    DepartmentDto create(DepartmentCreateRequest request);

    DepartmentDto update(Long id, DepartmentUpdateRequest request);

    void delete(Long id);
}