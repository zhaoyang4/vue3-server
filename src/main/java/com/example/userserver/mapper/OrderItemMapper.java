package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.userserver.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/** 订单明细数据访问层（基础 CRUD 由 BaseMapper 提供） */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
