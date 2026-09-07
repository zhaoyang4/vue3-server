package com.example.userserver.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.userserver.dto.CreateOrderDTO;
import com.example.userserver.entity.Order;
import com.example.userserver.vo.OrderDetailVO;
import com.example.userserver.vo.OrderVO;

public interface OrderService {

    /** 创建订单（事务内写 order + order_item，并乐观锁扣库存） */
    Long createOrder(CreateOrderDTO dto);

    /**
     * 订单列表分页（自定义 XML SQL，LEFT JOIN user 带出用户名/账号）。
     *
     * @param keyword 模糊匹配 订单号 / 用户姓名 / 用户账号（可空）
     * @param status  订单状态精确过滤（可空 = 全部状态）
     * @param userId  指定用户的订单（用户详情页的购买历史；可空）
     */
    IPage<OrderVO> pageQuery(long current, long size, String keyword, Integer status, Long userId);

    /** 订单详情（订单主信息 + 商品明细） */
    OrderDetailVO detail(Long id);

    /** 根据主键查订单（供支付/状态流转前校验使用） */
    Order getOrder(Long id);

    /**
     * 订单状态流转（带状态机校验 + CAS 原子更新）。
     * 只有 OrderStatusEnum 里声明的合法流转才允许（如 待支付→已支付），
     * 非法流转（如 待支付→已完成）直接抛异常；并发下状态被改则提示重试。
     *
     * @return 是否流转成功
     */
    boolean changeStatus(Long orderId, int targetStatus);
}
