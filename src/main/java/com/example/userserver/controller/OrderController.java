package com.example.userserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.userserver.common.Result;
import com.example.userserver.dto.CreateOrderDTO;
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
}
