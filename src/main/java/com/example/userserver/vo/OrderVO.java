package com.example.userserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表的「视图对象」VO。
 * 和 Order 实体的区别：多了 userName 字段，来自 LEFT JOIN user 表。
 * 前端订单列表要显示「谁下的单」，但 order 表只存了 user_id，
 * 所以在查询时用 LEFT JOIN 把 user.name 带出来，映射到这个 VO。
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;       // 左联 user 得到
    private BigDecimal totalAmount;
    private Integer status;        // 0待支付 1已支付 2已取消

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
