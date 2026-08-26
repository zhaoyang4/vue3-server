package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细实体（一个订单包含多个明细，一行就是一个商品）。
 * product_name / product_price 是「下单时快照」，避免商品后来改名/调价影响历史订单。
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;          // 所属订单ID
    private Long productId;        // 商品ID
    private String productName;    // 商品名称快照
    private BigDecimal productPrice; // 下单时单价快照
    private Integer quantity;      // 购买数量
    private BigDecimal subtotal;   // 小计 = 单价 × 数量
}
