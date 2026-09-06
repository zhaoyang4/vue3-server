package com.example.userserver.mapper;

import com.example.userserver.vo.ProductStockVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存预警的数据访问层（DAO）。
 *
 * ★ 本项目第一个「XML 映射」的 Mapper：
 *   方法签名写在这里，SQL 写在 resources/mapper/ProductStockMapper.xml 里。
 *   和 StatisticsMapper（用 @Select 注解写 SQL）对比着看，能直观体会
 *   「注解写简单 SQL」与「XML 写复杂 SQL」两种风格的取舍。
 *
 * 为什么这个查询更适合 XML？
 *   - 有动态条件（onlyAlert 为真才加 WHERE 过滤）；
 *   - 有 CASE 多分支、GREATEST 等计算列；
 *   - 语句较长，XML 里排版、加注释更清晰。
 *
 * 接口上不加 @Param 也行（MyBatis 3.4+ 可按参数名推断），
 * 但显式加 @Param 能让 XML 里的 #{threshold} 一目了然对应哪个参数，更稳。
 */
@Mapper
public interface ProductStockMapper {

    /**
     * 商品库存预警查询。
     *
     * @param threshold  预警阈值：库存低于它即视为「预警」（含「缺货」）
     * @param target     目标库存：补货补到这个量，建议补货量 = target - 当前库存
     * @param onlyAlert  true 只返回预警/缺货商品；false 返回全部（附带状态列）
     * @return 每个商品的库存预警视图
     */
    List<ProductStockVO> selectStockAlert(
            @Param("threshold") int threshold,
            @Param("target") int target,
            @Param("onlyAlert") boolean onlyAlert);
}
