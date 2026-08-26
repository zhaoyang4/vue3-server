package com.example.userserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品销量排行 VO（统计报表用）。
 * 由 product LEFT JOIN order_item 后 GROUP BY 得到。
 */
@Data
public class ProductSalesVO {

    private Long productId;
    private String productName;
    private Long totalQuantity;    // 销量（SUM order_item.quantity）
    private BigDecimal totalAmount; // 销售额（SUM order_item.subtotal）
}
