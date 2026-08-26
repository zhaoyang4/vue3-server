package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体。
 * 注意：数据库表名是 `order`，它是 MySQL 的保留字，
 * 所以这里 @TableName 必须用反引号包起来，否则 SQL 会报语法错。
 */
@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;        // 订单号（业务生成，唯一）
    private Long userId;           // 下单用户ID
    private BigDecimal totalAmount; // 订单总金额
    private Integer status;        // 0待支付 1已支付 2已取消

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
