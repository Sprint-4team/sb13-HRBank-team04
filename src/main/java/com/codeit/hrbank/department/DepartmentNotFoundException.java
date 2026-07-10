package com.codeit.hrbank.department;

public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(Long id) {
        super("부서를 찾을 수 없습니다: " + id);
    }
}