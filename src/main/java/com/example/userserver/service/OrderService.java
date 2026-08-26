package com.example.userserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.userserver.dto.CreateOrderDTO;
import com.example.userserver.vo.OrderDetailVO;
import com.example.userserver.vo.OrderVO;

public interface OrderService {

    /** 创建订单（事务内写 order + order_item，并乐观锁扣库存） */
    Long createOrder(CreateOrderDTO dto);

    /** 订单列表分页（LEFT JOIN user 带出用户名） */
    IPage<OrderVO> pageQuery(long current, long size, String keyword);

    /** 订单详情（订单主信息 + 商品明细） */
    OrderDetailVO detail(Long id);
}
