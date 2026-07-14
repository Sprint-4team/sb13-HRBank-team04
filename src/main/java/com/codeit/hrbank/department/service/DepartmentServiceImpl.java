package com.codeit.hrbank.department.service;

import com.codeit.hrbank.department.dto.*;
import com.codeit.hrbank.department.entity.Department;
import com.codeit.hrbank.department.exception.DepartmentHasEmployeesException;
import com.codeit.hrbank.department.exception.DepartmentNotFoundException;
import com.codeit.hrbank.department.exception.DuplicateDepartmentNameException;
import com.codeit.hrbank.department.repository.DepartmentRepository;
import com.codeit.hrbank.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public DepartmentDto create(DepartmentCreateRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new DuplicateDepartmentNameException(request.name());
        }

        Department department = Department.builder()
                .name(request.name())
                .description(request.description())
                .establishedDate(request.establishedDate())
                .build();

        Department saved = departmentRepository.save(department);

        long employeeCount = employeeRepository.countByDepartmentId(saved.getId());

        return new DepartmentDto(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getEstablishedDate(),
                employeeCount
        );
    }
    @Override
    @Transactional
    public DepartmentDto update(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        // 자기 자신을 제외하고 이름 중복 체크
        if (!department.getName().equals(request.name())
                && departmentRepository.existsByName(request.name())) {
            throw new DuplicateDepartmentNameException(request.name());
        }

        department.update(request.name(), request.description(), request.establishedDate());

        long employeeCount = employeeRepository.countByDepartmentId(department.getId());

        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getEstablishedDate(),
                employeeCount
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        long employeeCount = employeeRepository.countByDepartmentId(id);
        if (employeeCount > 0) {
            throw new DepartmentHasEmployeesException(id);
        }

        departmentRepository.delete(department);
    }

    @Override
    public DepartmentDto find(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));

        long employeeCount = employeeRepository.countByDepartmentId(department.getId());

        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getEstablishedDate(),
                employeeCount
        );
    }

    @Override
    public CursorPageResponseDepartmentDto findAll(DepartmentSearchCondition condition) {
        int size = condition.size() > 0 ? condition.size() : 10;

        List<Department> departments = departmentRepository.searchDepartments(condition);

        boolean hasNext = departments.size() > size;
        List<Department> content = hasNext ? departments.subList(0, size) : departments;

        List<DepartmentDto> dtoList = content.stream()
                .map(department -> {
                    long employeeCount = employeeRepository.countByDepartmentId(department.getId());
                    return new DepartmentDto(
                            department.getId(),
                            department.getName(),
                            department.getDescription(),
                            department.getEstablishedDate(),
                            employeeCount
                    );
                })
                .toList();

        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext && !content.isEmpty()) {
            Department last = content.get(content.size() - 1);
            String sortField = condition.sortField() != null ? condition.sortField() : "establishedDate";
            nextCursor = sortField.equals("name")
                    ? last.getName()
                    : last.getEstablishedDate().toString();
            nextIdAfter = last.getId();
        }

        long totalElements = departmentRepository.countDepartments(condition);

        return new CursorPageResponseDepartmentDto(
                dtoList,
                nextCursor,
                nextIdAfter,
                size,
                totalElements,
                hasNext
        );
    }

}