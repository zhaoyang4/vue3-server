package com.example.userserver.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.common.Result;
import com.example.userserver.entity.Product;
import com.example.userserver.service.ProductService;
import com.example.userserver.vo.ProductStockVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品接口控制器（Controller 层）。
 *
 * 路由设计（RESTful 风格，与用户模块保持一致）：
 *   POST   /api/products        新增商品（入库）
 *   GET    /api/products        分页查询（支持 keyword 按名称模糊搜索）
 *   GET    /api/products/{id}   根据 id 查单个
 *   PUT    /api/products/{id}   根据 id 修改
 *   DELETE /api/products/{id}   根据 id 删除
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    @Resource
    private ProductService productService;

    /** 入库：新增商品 */
    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        // 业务校验
        if (product.getName() == null || product.getName().isBlank()) {
            return Result.error("商品名称不能为空");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("价格不能为负数");
        }
        if (product.getPurchaseDate() == null) {
            return Result.error("购买日期不能为空");
        }
        // 库存校验：不传按 0 处理（DB 默认 100，这里显式兜底为 0，避免"没填却莫名有 100 件"）
        if (product.getStock() == null) {
            product.setStock(0);
        } else if (product.getStock() < 0) {
            return Result.error("库存不能为负数");
        }
        // 手动维护时间字段
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        productService.save(product);       // BaseMapper 的 insert
        return Result.success(product);     // 返回带主键的新对象
    }

    /** 分页查询商品列表 */
    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<Product> page = productService.pageQuery(current, size, keyword);
        return Result.success(page);
    }

    /** 根据 id 查询单个商品（用于"修改"时回显） */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    /**
     * 商品库存预警看板：自定义 SQL（product LEFT JOIN order_item + CASE 算状态）。
     * 这条接口挂在 /api/products 下面，属于「商品模块」的延伸功能。
     *
     * @param threshold  预警阈值，默认 10（库存低于它即预警/缺货）
     * @param target     目标库存，默认 50（建议补货量 = target - 当前库存）
     * @param onlyAlert  默认 true，只返回预警/缺货商品；传 false 返回全部（带状态列）
     */
    @GetMapping("/stock-alert")
    public Result<List<ProductStockVO>> stockAlert(
            @RequestParam(defaultValue = "10") int threshold,
            @RequestParam(defaultValue = "50") int target,
            @RequestParam(defaultValue = "true") boolean onlyAlert) {
        return Result.success(productService.stockAlert(threshold, target, onlyAlert));
    }

    /** 修改商品 */
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("价格不能为负数");
        }
        if (product.getStock() != null && product.getStock() < 0) {
            return Result.error("库存不能为负数");
        }
        product.setId(id);                  // 必须带上 id，MyBatis-Plus 才知道改哪条
        product.setUpdateTime(LocalDateTime.now());
        productService.updateById(product); // BaseMapper 的 update
        return Result.success(product);
    }

    /** 删除商品 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = productService.removeById(id);
        return Result.success(removed);
    }
}
