package com.codeit.hrbank.changelog.dto;

public record DiffDto(
        String propertyName,
        String before,
        String after
) {
}
