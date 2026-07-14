package com.codeit.hrbank.department.repository;

import com.codeit.hrbank.department.dto.DepartmentSearchCondition;
import com.codeit.hrbank.department.entity.Department;
import com.codeit.hrbank.department.entity.QDepartment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Department> searchDepartments(DepartmentSearchCondition condition) {
        QDepartment department = QDepartment.department;

        boolean isAsc = condition.sortDirection() == null
                || condition.sortDirection() == Sort.Direction.ASC;
        String sortField = condition.sortField() != null ? condition.sortField() : "establishedDate";

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(baseFilters(condition, department));

        BooleanExpression cursorCondition = buildCursorCondition(condition, department, sortField, isAsc);
        if (cursorCondition != null) {
            builder.and(cursorCondition);
        }

        OrderSpecifier<?>[] orderSpecifiers = buildOrderSpecifiers(department, sortField, isAsc);

        int size = condition.size() > 0 ? condition.size() : 10;

        return queryFactory
                .selectFrom(department)
                .where(builder)
                .orderBy(orderSpecifiers)
                .limit(size + 1L) // hasNext 판단을 위해 1개 더 조회
                .fetch();
    }

    private BooleanBuilder baseFilters(DepartmentSearchCondition condition, QDepartment department) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.nameOrDescription() != null) {
            builder.and(
                    department.name.contains(condition.nameOrDescription())
                            .or(department.description.contains(condition.nameOrDescription()))
            );
        }
        return builder;
    }

    // 복합 커서: (정렬값, id) < (마지막정렬값, 마지막id) 형태
    private BooleanExpression buildCursorCondition(DepartmentSearchCondition condition,
                                                   QDepartment department, String sortField, boolean isAsc) {

        if (condition.cursor() == null || condition.idAfter() == null) {
            return null;
        }

        Long idAfter = condition.idAfter();
        String cursor = condition.cursor();

        return switch (sortField) {
            case "establishedDate" -> {
                LocalDate lastDate = LocalDate.parse(cursor);
                yield isAsc
                        ? department.establishedDate.gt(lastDate)
                          .or(department.establishedDate.eq(lastDate).and(department.id.gt(idAfter)))
                        : department.establishedDate.lt(lastDate)
                          .or(department.establishedDate.eq(lastDate).and(department.id.lt(idAfter)));
            }
            default -> isAsc // name
                    ? department.name.gt(cursor)
                      .or(department.name.eq(cursor).and(department.id.gt(idAfter)))
                    : department.name.lt(cursor)
                      .or(department.name.eq(cursor).and(department.id.lt(idAfter)));
        };
    }

    private OrderSpecifier<?>[] buildOrderSpecifiers(QDepartment department, String sortField, boolean isAsc) {
        OrderSpecifier<?> primary = switch (sortField) {
            case "establishedDate" -> isAsc ? department.establishedDate.asc() : department.establishedDate.desc();
            default -> isAsc ? department.name.asc() : department.name.desc();
        };
        OrderSpecifier<?> secondary = isAsc ? department.id.asc() : department.id.desc();
        return new OrderSpecifier[]{primary, secondary};
    }

    @Override
    public long countDepartments(DepartmentSearchCondition condition) {
        QDepartment department = QDepartment.department;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(baseFilters(condition, department));

        Long count = queryFactory
                .select(department.count())
                .from(department)
                .where(builder)
                .fetchOne();

        return count != null ? count : 0L;
    }

}