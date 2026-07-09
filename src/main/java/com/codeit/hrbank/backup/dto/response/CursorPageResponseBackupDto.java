package com.codeit.hrbank.backup.dto.response;

import java.util.List;

public record CursorPageResponseBackupDto(
        List<BackupDto> content,
        String nextCursor,
        Long nextIdAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
//    public static CursorPageResponseBackupDto from() {
//        return new CursorPageResponseBackupDto(
//
//        );
//    }
}
