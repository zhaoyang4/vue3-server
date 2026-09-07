package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.vo.UserBalanceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户余额列表 Mapper（自定义 SQL，XML 实现）。
 *
 * 对应 resources/mapper/UserBalanceMapper.xml：
 *   user LEFT JOIN account，把每个用户的账户余额直接带进列表，
 * 前端用户管理页就能一列看到「余额」，不用再逐个查账户接口。
 */
@Mapper
public interface UserBalanceMapper {

    /**
     * 分页查询用户 + 余额。
     *
     * @param page     分页对象（由 MyBatis-Plus 分页插件自动改写为 LIMIT）
     * @param keyword  关键字，模糊匹配 账号/姓名/手机号（三者满足其一）
     */
    IPage<UserBalanceVO> selectUserBalancePage(
            Page<UserBalanceVO> page,
            @Param("keyword") String keyword);
}
