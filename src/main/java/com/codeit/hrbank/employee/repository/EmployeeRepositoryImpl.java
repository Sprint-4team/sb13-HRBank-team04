package com.codeit.hrbank.employee.repository;

import com.codeit.hrbank.employee.dto.EmployeeDistributionDto;
import com.codeit.hrbank.employee.dto.request.EmployeeSearchCondition;
import com.codeit.hrbank.employee.entity.Employee;
import com.codeit.hrbank.employee.entity.QEmployee;
import com.codeit.hrbank.department.entity.QDepartment;
import com.codeit.hrbank.employee.enums.EmployeeStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Employee> searchEmployees(EmployeeSearchCondition condition) {
    QEmployee employee = QEmployee.employee;
    QDepartment department = QDepartment.department;

    boolean isAsc = condition.sortDirection() == null
        || condition.sortDirection() == Sort.Direction.ASC;
    String sortField = condition.sortField() != null ? condition.sortField() : "name";

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(baseFilters(condition, employee, department));

    BooleanExpression cursorCondition = buildCursorCondition(
        condition, employee, sortField, isAsc);
    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }

    OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(employee, sortField, isAsc);

    int size = condition.size() > 0 ? condition.size() : 10;

    return queryFactory
        .selectFrom(employee)
        .leftJoin(employee.department, department).fetchJoin()
        .where(builder)
        .orderBy(orderSpecifiers)
        .limit(size + 1L) // hasNext 판단을 위해 1개 더 조회
        .fetch();
  }

  @Override
  public long countEmployees(EmployeeSearchCondition condition) {
    QEmployee employee = QEmployee.employee;
    QDepartment department = QDepartment.department;

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(baseFilters(condition, employee, department));

    Long count = queryFactory
        .select(employee.count())
        .from(employee)
        .leftJoin(employee.department, department)
        .where(builder)
        .fetchOne();
    return count != null ? count : 0L;
  }

  @Override
  public long countByCondition(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
    QEmployee employee = QEmployee.employee;
    BooleanBuilder builder = new BooleanBuilder();

    if (status != null) {
      builder.and(employee.status.eq(status));
    }
    if (fromDate != null) {
      builder.and(employee.hireDate.goe(fromDate));
    }
    if (toDate != null) {
      builder.and(employee.hireDate.loe(toDate));
    }

    Long count = queryFactory
        .select(employee.count())
        .from(employee)
        .where(builder)
        .fetchOne();

    return count != null ? count : 0L;
  }

  @Override
  public List<EmployeeDistributionDto> countGroupByField(String groupBy, EmployeeStatus status) {
    QEmployee employee = QEmployee.employee;
    QDepartment department = QDepartment.department;

    BooleanBuilder builder =new BooleanBuilder();
    if(status != null) {
      builder.and(employee.status.eq(status));
    }

    var groupExpr = "position".equalsIgnoreCase(groupBy)
        ? employee.position
        : department.name;
    List<com.querydsl.core.Tuple> results = queryFactory
        .select(groupExpr, employee.count())
        .from(employee)
        .leftJoin(employee.department, department)
        .where(builder)
        .groupBy(groupExpr)
        .fetch();

    long total = results.stream()
        .mapToLong(t -> t.get(employee.count()))
        .sum();

    return results.stream()
        .map(t -> {String key = t.get(groupExpr);
        Long count = t.get(employee.count());

        double percentage = total > 0
        ? Math.round((count * 10000.0 / total )) / 100.0 : 0.0;

        return new EmployeeDistributionDto(key, count, percentage);
        })
        .toList();

  }

  @Override
  public long countHiredBefore(LocalDate asOfDate) {
    QEmployee employee = QEmployee.employee;

    Long count = queryFactory
        .select(employee.count())
        .from(employee)
        .where(employee.hireDate.loe(asOfDate))
        .fetchOne();

    return count != null ? count : 0L;
  }

  private BooleanBuilder baseFilters(EmployeeSearchCondition condition,
      QEmployee employee, QDepartment department) {
    BooleanBuilder builder = new BooleanBuilder();

    if (condition.nameOrEmail() != null && !condition.nameOrEmail().isBlank()) {
      String keyword = condition.nameOrEmail().trim();
      builder.and(
          employee.name.contains(keyword)
              .or(employee.email.contains(keyword))
      );
    }
    if (condition.departmentName() != null && !condition.departmentName().isBlank()) {
      builder.and(department.name.contains(condition.departmentName().trim()));
    }
    if (condition.position() != null && !condition.position().isBlank()) {
      builder.and(employee.position.contains(condition.position().trim()));
    }
    if (condition.employeeNumber() != null && !condition.employeeNumber().isBlank()) {
      builder.and(employee.employeeNumber.contains(condition.employeeNumber().trim()));
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
    return builder;
  }

  // 복합 커서: (정렬값, id) < (마지막정렬값, 마지막id) 형태
  private BooleanExpression buildCursorCondition(EmployeeSearchCondition condition,
      QEmployee employee, String sortField, boolean isAsc) {

    if (condition.cursor() == null || condition.idAfter() == null) {
      return null;
    }

    Long idAfter = condition.idAfter();
    String cursor = condition.cursor();

    return switch (sortField) {
      case "hireDate" -> {
        LocalDate lastHireDate = LocalDate.parse(cursor);
        yield isAsc
            ? employee.hireDate.gt(lastHireDate)
              .or(employee.hireDate.eq(lastHireDate).and(employee.id.gt(idAfter)))
            : employee.hireDate.lt(lastHireDate)
              .or(employee.hireDate.eq(lastHireDate).and(employee.id.lt(idAfter)));
      }
      case "employeeNumber" -> isAsc
          ? employee.employeeNumber.gt(cursor)
            .or(employee.employeeNumber.eq(cursor).and(employee.id.gt(idAfter)))
          : employee.employeeNumber.lt(cursor)
            .or(employee.employeeNumber.eq(cursor).and(employee.id.lt(idAfter)));
      default -> isAsc // name
          ? employee.name.gt(cursor)
            .or(employee.name.eq(cursor).and(employee.id.gt(idAfter)))
          : employee.name.lt(cursor)
            .or(employee.name.eq(cursor).and(employee.id.lt(idAfter)));
    };
  }

  private OrderSpecifier<?>[] buildOrderSpecifiers(QEmployee employee, String sortField, boolean isAsc) {
    OrderSpecifier<?> primary = switch (sortField) {
      case "hireDate" -> isAsc ? employee.hireDate.asc() : employee.hireDate.desc();
      case "employeeNumber" -> isAsc ? employee.employeeNumber.asc() : employee.employeeNumber.desc();
      default -> isAsc ? employee.name.asc() : employee.name.desc();
    };
    OrderSpecifier<?> secondary = isAsc ? employee.id.asc() : employee.id.desc();
    return new OrderSpecifier[]{primary, secondary};
  }
}