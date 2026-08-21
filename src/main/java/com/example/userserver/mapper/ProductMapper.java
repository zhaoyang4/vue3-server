package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.userserver.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问层（DAO）。
 * 继承 BaseMapper<Product> 后，MyBatis-Plus 自动提供全套 CRUD 方法，
 * 无需自己写任何 SQL。
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 基础 CRUD 已由 BaseMapper 提供，无需任何代码
}
