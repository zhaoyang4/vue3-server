package com.example.userserver.service.impl;

import com.example.userserver.mapper.StatisticsMapper;
import com.example.userserver.service.StatisticsService;
import com.example.userserver.vo.ProductSalesVO;
import com.example.userserver.vo.UserConsumeVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;

    @Override
    public List<UserConsumeVO> consumeRank() {
        return statisticsMapper.consumeRank();
    }

    @Override
    public List<ProductSalesVO> salesRank() {
        return statisticsMapper.salesRank();
    }
}
