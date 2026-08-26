package com.example.userserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户消费排行 VO（统计报表用）。
 * 由 user LEFT JOIN order LEFT JOIN order_item 后 GROUP BY 得到。
 */
@Data
public class UserConsumeVO {

    private Long userId;
    private String userName;
    private Long orderCount;       // 订单数（COUNT DISTINCT order.id）
    private BigDecimal totalAmount; // 消费总额（SUM order_item.subtotal）
}
