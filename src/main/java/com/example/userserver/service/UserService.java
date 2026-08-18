package com.example.userserver.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.userserver.entity.User;

/**
 * 用户业务接口。
 *
 * 继承 IService<User> 后，已经白送了常用方法：
 *   save / saveBatch / getById / list / page / updateById / removeById ...
 * 我们只额外定义两个"业务专属"的方法。
 */
public interface UserService extends IService<User> {

    /**
     * 分页 + 关键字查询。
     * @param current 第几页（从 1 开始）
     * @param size    每页条数
     * @param keyword 关键字（匹配 账号/姓名/手机号，模糊查询；为空则查全部）
     * @return MyBatis-Plus 的分页对象，内含 records(本页数据) / total(总条数) 等
     */
    Page<User> pageQuery(long current, long size, String keyword);

    /**
     * 判断某个登录账号是否已存在（用于新增/修改时去重）。
     * @param username 待校验的账号
     * @param excludeId 修改时要排除自身 id（自己改自己时不算重复）
     */
    boolean usernameExists(String username, Long excludeId);
}
