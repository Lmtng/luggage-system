package com.luggage.luggagesystem.common;

import com.luggage.luggagesystem.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器
 *
 * 功能：
 * 1. 统一处理业务异常（BusinessException）
 * 2. 统一处理参数校验异常
 * 3. 统一处理数据库异常
 * 4. 统一处理未知异常
 *
 * 所有异常都转换为统一的 Result 格式返回给前端
 *
 * @author 成员B
 * @date 2026-09-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * 由 Service 层主动抛出的业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(Integer.parseInt(e.getCode()), e.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * 由 @Valid 注解触发的校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 获取第一个校验错误信息
        String message = e.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("参数校验失败");

        log.warn("参数校验失败: {}", message);
        return Result.error(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 处理数据库唯一索引冲突
     *
     * 如：订单号重复、用户名重复
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage());

        // 根据不同的错误信息返回不同的提示
        if (e.getMessage().contains("order_no")) {
            return Result.error(1001, "订单号已存在，请重试");
        }
        if (e.getMessage().contains("username")) {
            return Result.error(1001, "用户名已存在");
        }
        if (e.getMessage().contains("locker_code")) {
            return Result.error(1001, "柜体编码已存在");
        }

        return Result.error(1001, "数据重复，请检查");
    }

    /**
     * 处理数据库约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSQLIntegrityException(SQLIntegrityConstraintViolationException e) {
        log.warn("数据库约束异常: {}", e.getMessage());

        if (e.getMessage().contains("Duplicate entry")) {
            return Result.error(1001, "数据已存在，请勿重复提交");
        }

        return Result.error(1001, "数据操作失败，请检查输入");
    }

    /**
     * 处理其他未知异常
     *
     * 所有未被捕获的异常最终都会到这里
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.INTERNAL_ERROR);
    }
}