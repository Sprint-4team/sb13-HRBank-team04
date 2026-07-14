package com.codeit.hrbank.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        String message = e.getStatus() == HttpStatus.NOT_FOUND
                ? "요청한 리소스를 찾을 수 없습니다."
                : "잘못된 요청입니다.";

        ErrorResponse response = ErrorResponse.of(
                e.getStatus().value(),
                message,
                e.getMessage()
        );
        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "잘못된 요청입니다.",
                detail
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}