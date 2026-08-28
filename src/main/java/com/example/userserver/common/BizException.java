package com.example.userserver.common;

/**
 * 业务异常：余额不足、订单状态非法、参数不合法等「可预期」的业务错误。
 * 与 RuntimeException 的区别：语义清晰，GlobalExceptionHandler 单独捕获并返回友好提示，
 * 不会被当成「系统异常」处理。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
