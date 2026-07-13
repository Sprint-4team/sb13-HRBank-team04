package com.codeit.hrbank.employee.repository;

import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.entity.QEmployee;
import com.codeit.hrbank.department.QDepartment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Employee> searchEmployees(EmployeeSearchCondition condition) {
    QEmployee employee = QEmployee.employee;
    QDepartment department = QDepartment.department;

    BooleanBuilder builder = new BooleanBuilder();

    if (condition.nameOrEmail() != null) {
      builder.and(
          employee.name.contains(condition.nameOrEmail())
              .or(employee.email.contains(condition.nameOrEmail()))
      );
    }
    if (condition.departmentName() != null) {
      builder.and(department.name.contains(condition.departmentName()));
    }
    if (condition.position() != null) {
      builder.and(employee.position.contains(condition.position()));
    }
    if (condition.employeeNumber() != null) {
      builder.and(employee.employeeNumber.contains(condition.employeeNumber()));
    }
    if (condition.hireDateFrom() != null) {
      builder.and(employee.hireDate.goe(condition.hireDateFrom()));
    }
    if (condition.hireDateTo() != null) {
      builder.and(employee.hireDate.loe(condition.hireDateTo()));
    }
    if (condition.status() != null) {
      builder.and(employee.status.eq(condition.status()));
    }

    return queryFactory
        .selectFrom(employee)
        .leftJoin(employee.department, department)
        .where(builder)
        .fetch();
  }
}