package com.example.userserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品库存预警 VO（库存看板用）。
 *
 * 由自定义 SQL（product LEFT JOIN order_item + GROUP BY + CASE）聚合得到，
 * 不是 product 表的简单映射，所以放在 vo 包而不是 entity 包。
 *
 * 字段说明：
 * - productId / productName：商品标识（来自 product 表）
 * - price：单价（BigDecimal，避免精度丢失）
 * - stock：当前库存
 * - totalSold：累计销量（SUM order_item.quantity，没卖过为 0）
 * - totalAmount：累计销售额（SUM order_item.subtotal，没卖过为 0）
 * - stockStatus：库存状态，SQL 里用 CASE 算出来，取值 安全 / 预警 / 缺货
 * - suggestRestock：建议补货数量，SQL 里用 GREATEST(目标库存 - 当前库存, 0) 算出来
 */
@Data
public class ProductStockVO {

    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private Long totalSold;
    private BigDecimal totalAmount;
    private String stockStatus;
    private Integer suggestRestock;
}
