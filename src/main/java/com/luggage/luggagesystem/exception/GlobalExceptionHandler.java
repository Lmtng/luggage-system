package com.luggage.luggagesystem.exception;
import com.luggage.luggagesystem.exception.NoAvailableCellException;
import com.luggage.luggagesystem.dto.ApiErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求参数或业务数据不正确。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse>
    handleIllegalArgument(IllegalArgumentException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    /**
     * 当前用户状态不允许执行操作。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse>
    handleIllegalState(IllegalStateException exception) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }

    /**
     * JSON格式错误或枚举值错误。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse>
    handleUnreadableRequest() {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "请求内容格式错误"
        );
    }

    /**
     * URL参数类型错误。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse>
    handleTypeMismatch() {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "请求参数格式错误"
        );
    }

    /**
     * 数据库唯一约束或外键约束错误。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse>
    handleDataIntegrityViolation() {

        return buildResponse(
                HttpStatus.CONFLICT,
                "数据已经存在或违反数据库约束"
        );
    }

    /**
     * 为以后添加@Valid参数校验预留。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse>
    handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = "请求参数校验失败";

        if (exception.getBindingResult()
                .getFieldError() != null) {

            message = exception.getBindingResult()
                    .getFieldError()
                    .getDefaultMessage();
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        status.value(),
                        message,
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
    @ExceptionHandler(NoAvailableCellException.class)
    public ResponseEntity<ApiErrorResponse>
    handleNoAvailableCell(
            NoAvailableCellException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }
}