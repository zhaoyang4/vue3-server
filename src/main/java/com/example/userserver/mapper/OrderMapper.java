package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.entity.Order;
import com.example.userserver.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订单数据访问层。
 * 基础 CRUD 由 BaseMapper<Order> 提供；下面三个是「复杂 SQL」演示：
 *   1) selectOrderPage —— LEFT JOIN user，把用户名带进订单列表（多表联查 + 分页）
 *   2) decreaseStock   —— 乐观锁扣库存（并发安全，防超卖）
 *   3) updateStatus    —— CAS 原子更新订单状态（配合状态机，防止非法流转）
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 订单列表分页查询（LEFT JOIN user）。
     * ${ew.customSqlSegment} 是 MyBatis-Plus 占位符，会被替换为 Wrapper 生成的 WHERE 条件。
     * 列名用 AS 直接取驼峰别名（id→id, order_no→orderNo…），不依赖下划线转驼峰配置。
     * 分页由 MybatisPlusConfig 里的分页插件自动加 LIMIT 与 COUNT。
     */
    @Select("SELECT o.id AS id, o.order_no AS orderNo, o.user_id AS userId, " +
            "o.total_amount AS totalAmount, o.status AS status, o.create_time AS createTime, " +
            "u.name AS userName " +
            "FROM `order` o LEFT JOIN user u ON o.user_id = u.id ${ew.customSqlSegment}")
    IPage<OrderVO> selectOrderPage(Page<OrderVO> page, @Param(Constants.WRAPPER) Wrapper<Order> wrapper);

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
