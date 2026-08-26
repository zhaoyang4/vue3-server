package com.example.userserver.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建订单的入参对象（前端 POST /api/orders 的请求体）。
 * 前端只需传：哪个用户(userId) + 买了哪些商品(items: 商品ID + 数量)。
 * 订单号、总额、商品名称/单价快照都由后端计算/补全。
 */
@Data
public class CreateOrderDTO {

    private Long userId;

    private List<Item> items;

    /** 订单里的一个商品项（商品ID + 购买数量） */
    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}
