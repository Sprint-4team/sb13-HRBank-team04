package com.codeit.hrbank.department.controller;

import com.codeit.hrbank.department.dto.DepartmentCreateRequest;
import com.codeit.hrbank.department.dto.DepartmentDto;
import com.codeit.hrbank.department.dto.DepartmentUpdateRequest;
import com.codeit.hrbank.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

}