package com.example.userserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.userserver.entity.Product;
import com.example.userserver.vo.ProductStockVO;

import java.util.List;

/**
 * 商品业务接口。
 * 继承 IService<Product> 后白送了 save/getById/list/page/updateById/removeById 等方法，
 * 我们只额外定义「分页 + 关键字查询」这一个业务方法。
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页 + 关键字查询。
     * @param current 第几页（从 1 开始）
     * @param size    每页条数
     * @param keyword 关键字（匹配商品名称，模糊查询；为空则查全部）
     * @return MyBatis-Plus 分页对象，内含 records(本页数据) / total(总条数)
     */
    Page<Product> pageQuery(long current, long size, String keyword);

    /**
     * 商品库存预警。
     * 底层走自定义 XML SQL（product 表 LEFT JOIN order_item 聚合 + CASE 算状态）。
     *
     * @param threshold  预警阈值（库存低于它即预警，含缺货）
     * @param target     目标库存（补货补到的量，决定建议补货数量）
     * @param onlyAlert  true 只返回预警/缺货商品；false 返回全部（带状态列）
     * @return 库存预警视图列表，按库存升序（最缺的排最前）
     */
    List<ProductStockVO> stockAlert(int threshold, int target, boolean onlyAlert);
}
