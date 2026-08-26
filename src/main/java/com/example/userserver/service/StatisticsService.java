package com.example.userserver.service;

import com.example.userserver.vo.ProductSalesVO;
import com.example.userserver.vo.UserConsumeVO;

import java.util.List;

public interface StatisticsService {

    /** 用户消费排行 */
    List<UserConsumeVO> consumeRank();

    /** 商品销量 TOP */
    List<ProductSalesVO> salesRank();
}
