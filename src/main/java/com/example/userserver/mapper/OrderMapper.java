package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.entity.Order;
import com.example.userserver.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单数据访问层。
 * 基础 CRUD 由 BaseMapper<Order> 提供；下面是「复杂 SQL」演示：
 *   1) selectOrderPage  —— LEFT JOIN user 的多条件分页（自定义 SQL，XML 实现，
 *                          支持 订单号/用户名/账号 模糊 + 状态 + 用户ID 过滤）
 *   2) decreaseStock    —— 乐观锁扣库存（并发安全，防超卖）
 *   3) updateStatus     —— CAS 原子更新订单状态（配合状态机，防止非法流转）
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 订单列表分页查询（LEFT JOIN user，XML 实现：resources/mapper/OrderMapper.xml）。
     *
     * @param keyword  模糊匹配 订单号 / 用户姓名 / 用户账号（三者满足其一，可空）
     * @param status   订单状态精确过滤（可空 = 不过滤）
     * @param userId   指定用户的订单（用户详情页的「购买历史」用它）
     */
    IPage<OrderVO> selectOrderPage(
            Page<OrderVO> page,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 乐观锁扣减库存：
     *   WHERE id=? AND stock>=? AND version=?  —— 只有「库存够 且 版本号没被别人改过」才更新成功（影响行数=1）。
     *   如果 version 已变（并发下被别的线程改了）→ 影响行数=0 → 上层据此抛异常回滚，杜绝超卖。
     * 每次成功都把 version + 1，让下一次更新必须基于最新版本。
     */
    @Update("UPDATE product SET stock = stock - #{qty}, version = version + 1 " +
            "WHERE id = #{id} AND stock >= #{qty} AND version = #{ver}")
    int decreaseStock(@Param("id") Long id, @Param("qty") int qty, @Param("ver") int ver);

    /**
     * 订单状态流转（CAS 原子更新）：
     *   只有当数据库里 status 仍是 #{from} 时才改成 #{to}，返回影响行数。
     *   影响行数 0 = 状态已被别人改过（并发）→ 上层据此提示重试。
     *   配合 OrderStatusEnum 的状态机规则，杜绝「待支付」直接跳「已完成」这类非法流转。
     */
    @Update("UPDATE `order` SET status = #{to}, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{from}")
    int updateStatus(@Param("id") Long id, @Param("from") int from, @Param("to") int to);
}
