package com.luggage.luggagesystem.exception;

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
     * 处理成员B订单模块中的业务异常。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException exception) {

        HttpStatus status = determineBusinessStatus(exception.getCode());

        return buildResponse(status, exception.getMessage());
    }

    /**
     * 请求参数或业务数据不正确。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    /**
     * 当前用户状态不允许执行操作。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(
            IllegalStateException exception) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
    }

    /**
     * 没有可用柜格。
     */
    @ExceptionHandler(NoAvailableCellException.class)
    public ResponseEntity<ApiErrorResponse> handleNoAvailableCell(
            NoAvailableCellException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    /**
     * JSON格式错误或枚举值错误。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableRequest() {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "请求内容格式错误"
        );
    }

    /**
     * URL参数类型错误。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch() {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "请求参数格式错误"
        );
    }

    /**
     * @Valid参数校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = "请求参数校验失败";

        if (exception.getBindingResult().getFieldError() != null) {
            message = exception.getBindingResult()
                    .getFieldError()
                    .getDefaultMessage();
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /**
     * 数据库唯一约束或外键约束错误。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "数据已经存在或违反数据库约束"
        );
    }

    /**
     * 处理未预料到的异常，防止内部错误信息直接暴露。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownException(
            Exception exception) {

        exception.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "系统内部错误，请稍后重试"
        );
    }

    /**
     * 根据订单业务错误码选择HTTP状态码。
     */
    private HttpStatus determineBusinessStatus(String code) {

        if (BusinessException.ORDER_NOT_FOUND.equals(code)) {
            return HttpStatus.NOT_FOUND;
        }

        if (BusinessException.ORDER_NOT_OWNER.equals(code)) {
            return HttpStatus.FORBIDDEN;
        }

        if (BusinessException.CELL_OCCUPIED.equals(code)
                || BusinessException.NO_AVAILABLE_CELL.equals(code)
                || BusinessException.ORDER_ALREADY_COMPLETED.equals(code)
                || BusinessException.ORDER_STATUS_ERROR.equals(code)) {

            return HttpStatus.CONFLICT;
        }

        return HttpStatus.BAD_REQUEST;
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String message) {

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}