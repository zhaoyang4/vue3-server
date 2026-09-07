package com.example.userserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品销售流水的视图对象（一行 = 某商品在一个订单里的一次成交）。
 *
 * 数据来自 order_item JOIN `order` JOIN user：
 *   order_item 存了「买了几个、成交单价、小计」（下单时的快照），
 *   `order` 补「订单号、订单状态、下单时间」，
 *   user 补「谁买的」。
 * 商品详情页的「销售流水」表格直接渲染这个 VO。
 */
@Data
public class ProductFlowVO {

    private Long orderId;              // 订单ID
    private String orderNo;            // 订单号
    private Long userId;               // 购买用户ID
    private String userName;           // 购买用户姓名（JOIN user）
    private String userAccount;        // 购买用户账号（JOIN user）
    private Integer quantity;          // 购买数量
    private BigDecimal productPrice;   // 成交单价（下单时快照）
    private BigDecimal subtotal;       // 小计 = 单价 × 数量
    private Integer orderStatus;       // 订单状态（快照当时订单的状态）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;  // 下单时间
}
