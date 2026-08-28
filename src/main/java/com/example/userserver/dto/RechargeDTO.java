package com.example.userserver.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值入参。@NotNull / @DecimalMin 是 Bean Validation 注解，
 * 配合 Controller 上的 @Valid 生效，校验失败会抛 MethodArgumentNotValidException，
 * 被 GlobalExceptionHandler 捕获并转成友好提示（见全局异常处理）。
 */
@Data
public class RechargeDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于 0")
    private BigDecimal amount;

    private String remark;
}
