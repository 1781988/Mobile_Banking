package com.mobilebanking.platform.common.exception;

import com.mobilebanking.platform.common.api.ErrorResponse;
import com.mobilebanking.platform.common.web.RequestIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
        return build(exception.getErrorCode(), exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ErrorResponse> handleValidation(Exception exception, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (exception instanceof MethodArgumentNotValidException manve) {
            manve.getBindingResult().getFieldErrors()
                    .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        } else if (exception instanceof BindException bindException) {
            bindException.getBindingResult().getFieldErrors()
                    .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        }
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.defaultMessage(), request, errors);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.defaultMessage(), request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> handleDataConflict(DataIntegrityViolationException exception,
                                                      HttpServletRequest request) {
        log.warn("Data integrity conflict, requestId={}", MDC.get(RequestIdFilter.MDC_KEY));
        return build(ErrorCode.DATA_CONFLICT, ErrorCode.DATA_CONFLICT.defaultMessage(), request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnknown(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception, requestId={}", MDC.get(RequestIdFilter.MDC_KEY), exception);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.defaultMessage(), request, Map.of());
    }

    private ResponseEntity<ErrorResponse> build(ErrorCode code,
                                                String message,
                                                HttpServletRequest request,
                                                Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
                false,
                code.name(),
                message,
                MDC.get(RequestIdFilter.MDC_KEY),
                request.getRequestURI(),
                fieldErrors,
                Instant.now()
        );
        return ResponseEntity.status(code.status()).body(body);
    }
}
