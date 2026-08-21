package com.example.userserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.userserver.entity.Product;

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
}
