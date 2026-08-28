package com.example.userserver.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 余额支付入参。biz_no = 订单号，用于【幂等】：
 * 同一订单号重复支付，流水表 biz_no 唯一约束会拦下第二次，不会扣两次。
 */
@Data
public class PayDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于 0")
    private BigDecimal amount;
}
