package com.example.userserver.mapper;

import com.example.userserver.vo.ProductSalesVO;
import com.example.userserver.vo.UserConsumeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 统计报表数据访问层。
 * 两个查询都是「多表 LEFT JOIN + GROUP BY 聚合」的复杂 SQL 演示：
 *   - consumeRank：用户消费排行（LEFT JOIN order / order_item，按用户聚合）
 *   - salesRank：商品销量 TOP（LEFT JOIN order_item，按商品聚合）
 * 注意返回的是 VO 列表，不是实体，所以这里不继承 BaseMapper。
 */
@Mapper
public interface StatisticsMapper {

    /** 用户消费排行：每个用户的订单数 + 消费总额，按总额倒序 */
    @Select("SELECT u.id AS userId, u.name AS userName, " +
            "COUNT(DISTINCT o.id) AS orderCount, " +
            "COALESCE(SUM(oi.subtotal), 0) AS totalAmount " +
            "FROM user u " +
            "LEFT JOIN `order` o ON u.id = o.user_id " +
            "LEFT JOIN order_item oi ON o.id = oi.order_id " +
            "GROUP BY u.id, u.name " +
            "ORDER BY totalAmount DESC")
    List<UserConsumeVO> consumeRank();

    /** 商品销量 TOP：每个商品的销量 + 销售额，按销量倒序 */
    @Select("SELECT p.id AS productId, p.name AS productName, " +
            "COALESCE(SUM(oi.quantity), 0) AS totalQuantity, " +
            "COALESCE(SUM(oi.subtotal), 0) AS totalAmount " +
            "FROM product p " +
            "LEFT JOIN order_item oi ON p.id = oi.product_id " +
            "GROUP BY p.id, p.name " +
            "ORDER BY totalQuantity DESC")
    List<ProductSalesVO> salesRank();
}
