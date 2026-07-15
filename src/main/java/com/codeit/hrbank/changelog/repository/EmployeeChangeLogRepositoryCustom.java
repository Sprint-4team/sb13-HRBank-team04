package com.codeit.hrbank.changelog.repository;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.entity.EmployeeChangeLog;

import java.time.Instant;
import java.util.List;

public interface EmployeeChangeLogRepositoryCustom {

    /**
     * 직원 정보 수정 이력 목록을 커서 기반으로 조회합니다.
     *
     * @param employeeNumber 대상 직원 사번 검색어
     * @param type 이력 유형
     * @param memo 메모 검색어
     * @param ipAddress IP 주소 검색어
     * @param atFrom 조회 시작 시각
     * @param atTo 조회 종료 시각
     * @param idAfter 이전 페이지 마지막 요소 ID
     * @param cursor 이전 페이지 마지막 정렬값
     * @param size 페이지 크기
     * @param sortField 정렬 필드: at, ipAddress
     * @param sortDirection 정렬 방향: asc, desc
     * @return 조회 결과. 다음 페이지 확인을 위해 size + 1개까지 반환합니다.
     */
    List<EmployeeChangeLog> findChangeLogs(
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
    );

    /**
     * 동일한 검색 조건에 해당하는 전체 이력 개수를 조회합니다.
     */
    long countChangeLogs(
            String employeeNumber,
            EmployeeChangeType type,
            String memo,
            String ipAddress,
            Instant atFrom,
            Instant atTo
    );
}
