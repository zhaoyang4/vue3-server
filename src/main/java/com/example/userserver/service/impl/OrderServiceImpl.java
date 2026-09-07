package com.example.userserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.userserver.common.BizException;
import com.example.userserver.dto.CreateOrderDTO;
import com.example.userserver.entity.Order;
import com.example.userserver.entity.OrderItem;
import com.example.userserver.entity.Product;
import com.example.userserver.entity.User;
import com.example.userserver.enums.OrderStatusEnum;
import com.example.userserver.mapper.OrderItemMapper;
import com.example.userserver.mapper.OrderMapper;
import com.example.userserver.mapper.ProductMapper;
import com.example.userserver.mapper.UserMapper;
import com.example.userserver.service.OrderService;
import com.example.userserver.vo.OrderDetailVO;
import com.example.userserver.vo.OrderVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单业务实现。核心演示点：
 *   1) @Transactional —— 下单的「写订单 + 写明细 + 扣库存」必须原子，任一步失败整体回滚。
 *   2) 乐观锁扣库存 —— decreaseStock 带 version 条件，并发下不会超卖。
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Resource
    private ProductMapper productMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateOrderDTO dto) {
        if (dto.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("订单至少包含一个商品");
        }

        // 1) 生成订单号（时间戳 + 随机数，保证唯一）
        String orderNo = "NO" + System.currentTimeMillis() + (int) (Math.random() * 9000 + 1000);

        // 2) 先插订单空壳，拿到自增主键
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(dto.getUserId());
        order.setStatus(0); // 0 待支付
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(order);

        // 3) 循环处理每个商品：校验 → 扣库存（乐观锁）→ 写明细（带快照）
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> itemList = new ArrayList<>();
        for (CreateOrderDTO.Item it : dto.getItems()) {
            Product p = productMapper.selectById(it.getProductId());
            if (p == null) {
                throw new RuntimeException("商品不存在: " + it.getProductId());
            }
            int qty = (it.getQuantity() == null || it.getQuantity() <= 0) ? 1 : it.getQuantity();
            if (p.getStock() == null || p.getStock() < qty) {
                throw new RuntimeException("商品「" + p.getName() + "」库存不足");
            }

            // 3.1 乐观锁扣库存：基于当前 version，扣成功影响行数=1，否则=0（并发冲突）
            int ver = p.getVersion() == null ? 0 : p.getVersion();
            int rows = baseMapper.decreaseStock(p.getId(), qty, ver);
            if (rows == 0) {
                throw new RuntimeException("下单失败：商品「" + p.getName() + "」并发冲突，请重试");
            }

            // 3.2 小计 + 累计总额
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);

            // 3.3 订单明细（冗余快照，历史订单不受商品改名/调价影响）
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductPrice(p.getPrice());
            oi.setQuantity(qty);
            oi.setSubtotal(subtotal);
            itemList.add(oi);
        }

        // 4) 批量插入明细
        for (OrderItem oi : itemList) {
            orderItemMapper.insert(oi);
        }

        // 5) 回写订单总额
        order.setTotalAmount(total);
        baseMapper.updateById(order);

        return order.getId();
    }

    @Override
    public IPage<OrderVO> pageQuery(long current, long size, String keyword, Integer status, Long userId) {
        // 自定义 XML SQL：LEFT JOIN user + 动态条件（订单号/用户名/账号模糊、状态、用户ID）
        Page<OrderVO> page = new Page<>(current, size);
        return baseMapper.selectOrderPage(page, keyword, status, userId);
    }

    @Override
    public OrderDetailVO detail(Long id) {
        Order order = baseMapper.selectById(id);
        if (order == null) {
            return null;
        }
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());
        User u = userMapper.selectById(order.getUserId());
        vo.setUserName(u != null ? u.getName() : null);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

        OrderDetailVO d = new OrderDetailVO();
        d.setOrder(vo);
        d.setItems(items);
        return d;
    }

    @Override
    public Order getOrder(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean changeStatus(Long orderId, int target) {
        Order order = baseMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在: " + orderId);
        }
        int from = order.getStatus();
        if (from == target) {
            return true; // 已是目标状态，幂等返回
        }
        // 1) 状态机校验：不在 OrderStatusEnum 声明的流转集合里 → 非法
        if (!OrderStatusEnum.canTransition(from, target)) {
            throw new BizException("非法状态流转：" + OrderStatusEnum.descOf(from)
                    + " → " + OrderStatusEnum.descOf(target));
        }
        // 2) CAS 原子更新：只有 DB 里 status 仍是 from 才改，并发被改则影响行数 0
        int rows = baseMapper.updateStatus(orderId, from, target);
        if (rows == 0) {
            throw new BizException("订单状态已被他人修改，请刷新后重试");
        }
        return true;
    }
}
