package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.vo.ProductFlowVO;
import com.example.userserver.vo.ProductSoldSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品销售流水 Mapper（自定义 SQL，XML 实现）。
 *
 * 对应 resources/mapper/ProductFlowMapper.xml：
 *   1) selectProductFlows   某商品在所有订单里的成交明细（分页）
 *   2) selectProductSummary 某商品的销量/销售额汇总（一条聚合 SQL）
 */
@Mapper
public interface ProductFlowMapper {

    /**
     * 分页查某商品的销售流水。
     *
     * @param productId 商品ID
     * @param keyword   可选，模糊匹配 订单号 / 买家姓名 / 买家账号
     */
    IPage<ProductFlowVO> selectProductFlows(
            Page<ProductFlowVO> page,
            @Param("productId") Long productId,
            @Param("keyword") String keyword);

    /**
     * 某商品的销售汇总（聚合）：累计销量 / 累计销售额 / 成交订单数。
     */
    ProductSoldSummaryVO selectProductSummary(@Param("productId") Long productId);
}
