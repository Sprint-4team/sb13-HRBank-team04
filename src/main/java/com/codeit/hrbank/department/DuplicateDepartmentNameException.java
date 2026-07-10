package com.codeit.hrbank.department;

public class DuplicateDepartmentNameException extends RuntimeException {

    public DuplicateDepartmentNameException(String name) {
        super("이미 존재하는 부서 이름입니다: " + name);
    }
}