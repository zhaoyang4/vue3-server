package com.example.userserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.userserver.entity.User;
import com.example.userserver.mapper.UserMapper;
import com.example.userserver.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户业务实现类。
 *
 * ServiceImpl<UserMapper, User> 已经实现了 IService 的全部默认方法，
 * 我们只要补上自己在接口里定义的 pageQuery / usernameExists 即可。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Page<User> pageQuery(long current, long size, String keyword) {
        // 1) 构造分页对象（MyBatis-Plus 分页插件会拦截并自动改写为 LIMIT 分页 SQL）
        Page<User> page = new Page<>(current, size);

        // 2) 构造查询条件。LambdaQueryWrapper 用方法引用写字段，编译期就能检查字段名，比字符串安全。
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            // 关键字匹配：账号 或 姓名 或 手机号，三者满足其一即可
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getName, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }
        // 按 id 倒序，新加的用户排前面
        wrapper.orderByDesc(User::getId);

        // 3) 返回分页结果（records=列表，total=总数）
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean usernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        // 修改场景：排除当前这条记录自身，避免"自己改自己"被判重
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        return baseMapper.selectCount(wrapper) > 0;
    }
}
