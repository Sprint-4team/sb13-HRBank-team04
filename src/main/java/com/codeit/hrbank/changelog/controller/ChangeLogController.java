package com.codeit.hrbank.changelog.controller;

import com.codeit.hrbank.changelog.EmployeeChangeType;
import com.codeit.hrbank.changelog.dto.ChangeLogDetailDto;
import com.codeit.hrbank.changelog.dto.CursorPageResponseChangeLogDto;
import com.codeit.hrbank.changelog.service.ChangeLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class ChangeLogController {

    private final ChangeLogService changeLogService;

    @GetMapping
    public CursorPageResponseChangeLogDto findChangeLogs(
            @RequestParam(required = false) String employeeNumber,
            @RequestParam(required = false) EmployeeChangeType type,
            @RequestParam(required = false) String memo,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) Instant atFrom,
            @RequestParam(required = false) Instant atTo,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "at") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return changeLogService.findChangeLogs(
                employeeNumber,
                type,
                memo,
                ipAddress,
                atFrom,
                atTo,
                idAfter,
                cursor,
                size,
                sortField,
                sortDirection
        );
    }

    @GetMapping("/{id}")
    public ChangeLogDetailDto findChangeLogDetail(
            @PathVariable Long id
    ) {
        return changeLogService.findChangeLogDetail(id);
    }

    @GetMapping("/count")
    public Long findChangeLogDetailCount(
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate
    ) {
        return changeLogService.findChangeLogDetailCount(fromDate, toDate);
    }

}
