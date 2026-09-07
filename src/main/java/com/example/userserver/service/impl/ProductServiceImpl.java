package com.example.userserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.userserver.entity.Product;
import com.example.userserver.mapper.ProductFlowMapper;
import com.example.userserver.mapper.ProductMapper;
import com.example.userserver.mapper.ProductStockMapper;
import com.example.userserver.service.ProductService;
import com.example.userserver.vo.ProductFlowVO;
import com.example.userserver.vo.ProductSoldSummaryVO;
import com.example.userserver.vo.ProductStockVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品业务实现类。
 * ServiceImpl<ProductMapper, Product> 已实现 IService 的默认方法，
 * 我们只需补上自己在接口里定义的 pageQuery。
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    // 库存预警专用 Mapper（自定义 XML SQL）。这里注入后即可在 stockAlert 里调用。
    @Resource
    private ProductStockMapper productStockMapper;

    // 商品销售流水专用 Mapper（自定义 XML SQL）
    @Resource
    private ProductFlowMapper productFlowMapper;

    @Override
    public Page<Product> pageQuery(long current, long size, String keyword) {
        // 1) 构造分页对象（分页插件自动改写为 LIMIT 分页 SQL）
        Page<Product> page = new Page<>(current, size);

        // 2) 构造查询条件：关键字模糊匹配商品名称
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getName, keyword);
        }
        // 按 id 倒序，最新入库的商品排前面
        wrapper.orderByDesc(Product::getId);

        // 3) 返回分页结果
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<ProductStockVO> stockAlert(int threshold, int target, boolean onlyAlert) {
        // 库存预警是「自定义 SQL」查询，交给专用的 ProductStockMapper（SQL 写在 XML 里）
        return productStockMapper.selectStockAlert(threshold, target, onlyAlert);
    }

    @Override
    public IPage<ProductFlowVO> productFlows(long current, long size, Long productId, String keyword) {
        // 某商品的销售流水分页（自定义 XML SQL：order_item JOIN order JOIN user）
        Page<ProductFlowVO> page = new Page<>(current, size);
        return productFlowMapper.selectProductFlows(page, productId, keyword);
    }

    @Override
    public ProductSoldSummaryVO productSoldSummary(Long productId) {
        // 某商品的销量/销售额聚合汇总（一条 SQL 算三个数）
        return productFlowMapper.selectProductSummary(productId);
    }
}
