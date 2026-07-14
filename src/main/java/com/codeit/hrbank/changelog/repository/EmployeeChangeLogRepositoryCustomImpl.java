package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;
import com.codeit.hrbank.changelog.entity.QEmployeeChangeLog;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmployeeChangeLogRepositoryCustomImpl
        implements EmployeeChangeLogRepositoryCustom {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String SORT_FIELD_AT = "at";
    private static final String SORT_FIELD_IP_ADDRESS = "ipAddress";
    private static final String SORT_DIRECTION_ASC = "asc";

    private final JPAQueryFactory queryFactory;
    private final QEmployeeChangeLog employeeChangeLog = QEmployeeChangeLog.employeeChangeLog;

    @Override
    public List<EmployeeChangeLog> findChangeLogs(
            String employeeNumber,
            EmployeeChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo,
            Long idAfter,
            String cursor,
            Integer size,
            String sortField,
            String sortDirection
    ) {
        int pageSize = resolvePageSize(size);
        String appliedSortField = resolveSortField(sortField);
        boolean ascending = isAscending(sortDirection);

        BooleanBuilder conditions = createSearchConditions(
                employeeNumber,
                type,
                memo,
                ipAddress,
                atFrom,
                atTo
        );

        BooleanExpression cursorCondition = createCursorCondition(
                idAfter,
                cursor,
                appliedSortField,
                ascending
        );

        if (cursorCondition != null) {
            conditions.and(cursorCondition);
        }

        OrderSpecifier<?> primaryOrder = createPrimaryOrder(
                appliedSortField,
                ascending
        );

        OrderSpecifier<?> idOrder = ascending
                ? employeeChangeLog.id.asc()
                : employeeChangeLog.id.desc();

        /*
         * size보다 1개 더 조회합니다.
         *
         * 예:
         * 요청 size가 10이면 11개까지 조회합니다.
         * Service에서 조회 결과가 11개라면 hasNext=true로 판단하고,
         * 실제 응답에는 앞의 10개만 담습니다.
         */
        return queryFactory
                .selectFrom(employeeChangeLog)
                .where(conditions)
                .orderBy(primaryOrder, idOrder)
                .limit(pageSize + 1L)
                .fetch();
    }

    @Override
    public long countChangeLogs(
            String employeeNumber,
            EmployeeChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo
    ) {
        BooleanBuilder conditions = createSearchConditions(
                employeeNumber,
                type,
                memo,
                ipAddress,
                atFrom,
                atTo
        );

        Long count = queryFactory
                .select(employeeChangeLog.count())
                .from(employeeChangeLog)
                .where(conditions)
                .fetchOne();

        return count == null ? 0L : count;
    }

    /**
     * 목록 조회와 전체 개수 조회에서 공통으로 사용하는 검색 조건입니다.
     */
    private BooleanBuilder createSearchConditions(
            String employeeNumber,
            EmployeeChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo
    ) {
        BooleanBuilder conditions = new BooleanBuilder();

        if (hasText(employeeNumber)) {
            conditions.and(
                    employeeChangeLog.employeeNumber.contains(
                            employeeNumber.trim()
                    )
            );
        }

        if (type != null) {
            conditions.and(employeeChangeLog.type.eq(type));
        }

        if (hasText(memo)) {
            conditions.and(
                    employeeChangeLog.memo.contains(memo.trim())
            );
        }

        if (hasText(ipAddress)) {
            conditions.and(
                    employeeChangeLog.ipAddress.contains(ipAddress.trim())
            );
        }

        if (atFrom != null) {
            conditions.and(
                    employeeChangeLog.createdAt.goe(atFrom)
            );
        }

        if (atTo != null) {
            conditions.and(
                    employeeChangeLog.createdAt.loe(atTo)
            );
        }

        return conditions;
    }

    /**
     * 커서 다음에 위치한 데이터만 조회하도록 조건을 만듭니다.
     *
     * cursor는 이전 페이지 마지막 데이터의 정렬 필드 값이고,
     * idAfter는 이전 페이지 마지막 데이터의 ID입니다.
     *
     * 동일한 createdAt 또는 동일한 ipAddress가 여러 건 존재할 수 있으므로
     * ID를 보조 정렬 기준으로 사용합니다.
     */
    private BooleanExpression createCursorCondition(
            Long idAfter,
            String cursor,
            String sortField,
            boolean ascending
    ) {
        if (!hasText(cursor)) {
            return null;
        }

        if (SORT_FIELD_IP_ADDRESS.equals(sortField)) {
            return createIpAddressCursorCondition(
                    cursor.trim(),
                    idAfter,
                    ascending
            );
        }

        return createCreatedAtCursorCondition(
                cursor.trim(),
                idAfter,
                ascending
        );
    }

    private BooleanExpression createCreatedAtCursorCondition(
            String cursor,
            Long idAfter,
            boolean ascending
    ) {
        Instant cursorAt = parseInstantCursor(cursor);

        BooleanExpression primaryCondition = ascending
                ? employeeChangeLog.createdAt.gt(cursorAt)
                : employeeChangeLog.createdAt.lt(cursorAt);

        if (idAfter == null) {
            return primaryCondition;
        }

        BooleanExpression sameValueIdCondition =
                employeeChangeLog.createdAt.eq(cursorAt)
                        .and(
                                ascending
                                        ? employeeChangeLog.id.gt(idAfter)
                                        : employeeChangeLog.id.lt(idAfter)
                        );

        return primaryCondition.or(sameValueIdCondition);
    }

    private BooleanExpression createIpAddressCursorCondition(
            String cursor,
            Long idAfter,
            boolean ascending
    ) {
        BooleanExpression primaryCondition = ascending
                ? employeeChangeLog.ipAddress.gt(cursor)
                : employeeChangeLog.ipAddress.lt(cursor);

        if (idAfter == null) {
            return primaryCondition;
        }

        BooleanExpression sameValueIdCondition =
                employeeChangeLog.ipAddress.eq(cursor)
                        .and(
                                ascending
                                        ? employeeChangeLog.id.gt(idAfter)
                                        : employeeChangeLog.id.lt(idAfter)
                        );

        return primaryCondition.or(sameValueIdCondition);
    }

    private OrderSpecifier<?> createPrimaryOrder(
            String sortField,
            boolean ascending
    ) {
        if (SORT_FIELD_IP_ADDRESS.equals(sortField)) {
            return ascending
                    ? employeeChangeLog.ipAddress.asc()
                    : employeeChangeLog.ipAddress.desc();
        }

        return ascending
                ? employeeChangeLog.createdAt.asc()
                : employeeChangeLog.createdAt.desc();
    }

    private Instant parseInstantCursor(String cursor) {
        try {
            return Instant.parse(cursor);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "at 정렬 cursor는 ISO-8601 형식이어야 합니다. cursor: "
                            + cursor,
                    exception
            );
        }
    }

    private int resolvePageSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_PAGE_SIZE;
        }

        return size;
    }

    private String resolveSortField(String sortField) {
        if (SORT_FIELD_IP_ADDRESS.equals(sortField)) {
            return SORT_FIELD_IP_ADDRESS;
        }

        return SORT_FIELD_AT;
    }

    private boolean isAscending(String sortDirection) {
        return SORT_DIRECTION_ASC.equalsIgnoreCase(sortDirection);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
