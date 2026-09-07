package com.example.userserver.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.common.Result;
import com.example.userserver.entity.User;
import com.example.userserver.service.UserService;
import com.example.userserver.vo.UserBalanceVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户接口控制器（Controller 层）。
 *
 * 职责：接收 HTTP 请求 -> 调用 Service 处理业务 -> 返回统一格式 Result。
 * 这里不涉及 SQL，只做"请求进来、结果出去"的调度。
 *
 * 路由设计（RESTful 风格）：
 *   POST   /api/users        新增用户
 *   GET    /api/users        分页查询（支持 keyword 模糊搜索）
 *   GET    /api/users/{id}   根据 id 查单个
 *   PUT    /api/users/{id}   根据 id 修改
 *   DELETE /api/users/{id}   根据 id 删除
 *
 * 关于跨域：原先这里挂了 @CrossOrigin（等于对全网放行），已移除。
 *          现在统一由 config/CorsConfig 全局配置，放行名单来自 app.cors.allowed-origins，
 *          可用环境变量 CORS_ALLOWED_ORIGINS 覆盖 —— 新增 Controller 不用再操心跨域。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    // @Resource 按类型注入 UserService 的实现（UserServiceImpl）
    @Resource
    private UserService userService;

    /** 新增用户 */
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        // 业务校验：账号不能重复
        if (userService.usernameExists(user.getUsername(), null)) {
            return Result.error("账号已存在：" + user.getUsername());
        }
        // 手动维护时间字段（也可用 MyBatis-Plus 自动填充，这里手写更直观）
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userService.save(user);          // BaseMapper 的 insert
        return Result.success(user);     // 返回带主键的新对象
    }

    /** 分页查询用户列表。 */
    @GetMapping
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<User> page = userService.pageQuery(current, size, keyword);
        return Result.success(page);
    }

    /**
     * 分页查询用户 + 余额（自定义 SQL：user LEFT JOIN account）。
     * 用户管理页的列表直接带出每个人的账户余额，未开户的用户显示 0。
     */
    @GetMapping("/with-balance")
    public Result<IPage<UserBalanceVO>> listWithBalance(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageQueryWithBalance(current, size, keyword));
    }

    /** 根据 id 查询单个用户（用于"修改"时回显数据） */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /** 修改用户 */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        if (userService.usernameExists(user.getUsername(), id)) {
            return Result.error("账号已存在：" + user.getUsername());
        }
        user.setId(id);                  // 必须带上 id，MyBatis-Plus 才知道改哪条
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);    // BaseMapper 的 update
        return Result.success(user);
    }

    /** 删除用户 */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean removed = userService.removeById(id);
        return Result.success(removed);
    }
}
