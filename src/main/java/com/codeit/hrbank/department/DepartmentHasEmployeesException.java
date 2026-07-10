package com.codeit.hrbank.department;

public class DepartmentHasEmployeesException extends RuntimeException {

    public DepartmentHasEmployeesException(Long id) {
        super("소속된 직원이 있어 부서를 삭제할 수 없습니다: " + id);
    }
}