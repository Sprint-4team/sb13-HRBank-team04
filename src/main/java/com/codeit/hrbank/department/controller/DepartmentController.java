package com.codeit.hrbank.department.controller;

import com.codeit.hrbank.department.dto.*;
import com.codeit.hrbank.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<DepartmentDto> create(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentDto response = departmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        DepartmentDto response = departmentService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> find(@PathVariable("id") Long id) {
        DepartmentDto response = departmentService.find(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseDepartmentDto> findAll(
            @RequestParam(required = false) String nameOrDescription,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) Sort.Direction sortDirection
    ) {
        DepartmentSearchCondition condition = new DepartmentSearchCondition(
                nameOrDescription, idAfter, cursor, size, sortField, sortDirection
        );
        CursorPageResponseDepartmentDto response = departmentService.findAll(condition);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}