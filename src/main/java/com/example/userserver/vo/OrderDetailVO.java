package com.example.userserver.vo;

import com.example.userserver.entity.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 订单详情 VO：订单主信息 + 商品明细列表。
 * 前端「订单详情页」直接消费这个结构。
 */
@Data
public class OrderDetailVO {
    private OrderVO order;
    private List<OrderItem> items;
}
