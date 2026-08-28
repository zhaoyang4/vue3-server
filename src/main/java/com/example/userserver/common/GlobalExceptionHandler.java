package com.example.userserver.common;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器（@RestControllerAdvice = 对所有 @RestController 生效）。
 *
 * 为什么需要它：
 *   1) 不用在每个 Controller 里写 try-catch，统一在这里兜底。
 *   2) @Valid 校验失败抛 MethodArgumentNotValidException → 把字段错误拼成可读提示。
 *   3) 业务异常 BizException → 直接透传业务文案。
 *   4) 唯一键冲突 DuplicateKeyException → 幂等场景（重复支付）下友好提示「请勿重复提交」。
 *
 * 所有异常最终都包成统一 Result{code:1,message,...}，前端一套逻辑处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** @Valid 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(msg);
    }

    /** 业务异常（余额不足、状态非法等） */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.error(e.getMessage());
    }

    /** 唯一约束冲突（幂等：重复支付同一 biz_no 落库被拦截） */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicate(DuplicateKeyException e) {
        return Result.error("操作重复，请勿重复提交");
    }

    /** 兜底：其它未捕获异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        return Result.error("系统异常：" + e.getMessage());
    }
}
