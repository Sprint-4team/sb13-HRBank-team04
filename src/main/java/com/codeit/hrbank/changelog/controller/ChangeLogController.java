package com.codeit.hrbank.changelog.controller;

import com.codeit.hrbank.changelog.dto.ChangeLogDetailDto;
import com.codeit.hrbank.changelog.dto.CursorPageResponseChangeLogDto;
import com.codeit.hrbank.changelog.dto.request.ChangeLogSearchCondition;
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
            @ModelAttribute ChangeLogSearchCondition condition
    ) {
        return changeLogService.findChangeLogs(condition);
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
