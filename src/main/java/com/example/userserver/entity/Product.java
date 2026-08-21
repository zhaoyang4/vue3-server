package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品实体类，与数据库表 `product` 一一对应。
 *
 * 字段说明：
 * - id：主键，数据库自增
 * - name：商品名称
 * - price：价格（用 BigDecimal 存钱，避免 double 精度丢失）
 * - purchaseDate：购买日期（LocalDate，格式 yyyy-MM-dd）
 * - createTime / updateTime：由后端维护，序列化时格式化为东八区时间
 *
 * 命名约定：Java 驼峰（purchaseDate）对应数据库下划线（purchase_date），
 * MyBatis-Plus 默认开启驼峰转下划线映射，无需额外配置。
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;                // 主键

    private String name;            // 商品名称
    private BigDecimal price;       // 价格
    private LocalDate purchaseDate; // 购买日期

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;  // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;  // 更新时间
}
