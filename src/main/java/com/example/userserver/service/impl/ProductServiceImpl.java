package com.example.userserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.userserver.entity.Product;
import com.example.userserver.mapper.ProductMapper;
import com.example.userserver.service.ProductService;
import org.springframework.stereotype.Service;

/**
 * 商品业务实现类。
 * ServiceImpl<ProductMapper, Product> 已实现 IService 的默认方法，
 * 我们只需补上自己在接口里定义的 pageQuery。
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

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
}
