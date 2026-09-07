package com.example.userserver.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品销售汇总 VO（一条聚合 SQL 的结果）。
 * 商品详情页顶部的汇总卡片用它：累计销量 / 累计销售额 / 成交订单数。
 */
@Data
public class ProductSoldSummaryVO {

    private Integer orderCount;       // 成交订单数（去重后的订单个数）
    private Integer totalQuantity;    // 累计销量（SUM quantity）
    private BigDecimal totalAmount;   // 累计销售额（SUM subtotal）
}
