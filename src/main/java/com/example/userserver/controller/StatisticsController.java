package com.example.userserver.controller;

import com.example.userserver.common.Result;
import com.example.userserver.service.StatisticsService;
import com.example.userserver.vo.ProductSalesVO;
import com.example.userserver.vo.UserConsumeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /** 用户消费排行 */
    @GetMapping("/user-consume")
    public Result<List<UserConsumeVO>> userConsume() {
        return Result.success(statisticsService.consumeRank());
    }

    /** 商品销量 TOP */
    @GetMapping("/product-sales")
    public Result<List<ProductSalesVO>> productSales() {
        return Result.success(statisticsService.salesRank());
    }
}
