package com.example.userserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.userserver.common.BizException;
import com.example.userserver.common.Result;
import com.example.userserver.dto.CreateOrderDTO;
import com.example.userserver.dto.PayDTO;
import com.example.userserver.entity.Account;
import com.example.userserver.entity.Order;
import com.example.userserver.enums.OrderStatusEnum;
import com.example.userserver.service.AccountService;
import com.example.userserver.service.OrderService;
import com.example.userserver.vo.OrderDetailVO;
import com.example.userserver.vo.OrderVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private AccountService accountService;

    /** 创建订单：事务 + 扣库存，失败返回错误信息 */
    @PostMapping
    public Result<Long> create(@RequestBody CreateOrderDTO dto) {
        try {
            return Result.success(orderService.createOrder(dto));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 订单列表（LEFT JOIN user，分页） */
    @GetMapping
    public Result<IPage<OrderVO>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(orderService.pageQuery(current, size, keyword));
    }

    /** 订单详情（含商品明细） */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(id));
    }

    /**
     * 余额支付订单：编排「扣账户余额（幂等）」+「订单状态流转（待支付→已支付）」。
     * 两个动作各自幂等：即便网络重试导致本接口被调多次，也不会重复扣钱。
     */
    @PostMapping("/{id}/pay")
    public Result<Account> pay(@PathVariable Long id, @RequestBody PayDTO dto) {
        try {
            Order order = orderService.getOrder(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            if (order.getStatus() != OrderStatusEnum.PENDING.getCode()) {
                return Result.error("订单状态不允许支付（需为待支付）");
            }
            if (!order.getUserId().equals(dto.getUserId())) {
                return Result.error("订单与用户不匹配");
            }
            if (order.getTotalAmount().compareTo(dto.getAmount()) != 0) {
                return Result.error("支付金额与订单不符");
            }
            dto.setOrderId(id);
            if (dto.getOrderNo() == null) {
                dto.setOrderNo(order.getOrderNo());
            }
            accountService.pay(dto); // 幂等扣余额 + 写支付流水
            orderService.changeStatus(id, OrderStatusEnum.PAID.getCode()); // 待支付→已支付
            return Result.success(accountService.getAccount(dto.getUserId()));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 发货：已支付 → 已发货 */
    @PostMapping("/{id}/ship")
    public Result<Boolean> ship(@PathVariable Long id) {
        try {
            return Result.success(orderService.changeStatus(id, OrderStatusEnum.SHIPPED.getCode()));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 确认完成：已发货 → 已完成 */
    @PostMapping("/{id}/complete")
    public Result<Boolean> complete(@PathVariable Long id) {
        try {
            return Result.success(orderService.changeStatus(id, OrderStatusEnum.COMPLETED.getCode()));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 取消订单：待支付/已支付 → 已取消 */
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancel(@PathVariable Long id) {
        try {
            return Result.success(orderService.changeStatus(id, OrderStatusEnum.CANCELLED.getCode()));
        } catch (BizException e) {
            return Result.error(e.getMessage());
        }
    }
}
