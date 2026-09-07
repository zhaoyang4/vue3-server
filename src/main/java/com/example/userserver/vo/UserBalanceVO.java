package com.example.userserver.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户列表（带余额）的视图对象。
 *
 * 和 User 实体的区别：多了 balance 字段，来自 LEFT JOIN account 表。
 * 余额在独立的 account 表（一个用户一个账户），用户列表想直接看到余额，
 * 就在查询时用 LEFT JOIN 把 account.balance 带出来，映射到这个 VO。
 * COALESCE(a.balance, 0)：还没开户的用户显示 0，而不是 null。
 */
@Data
public class UserBalanceVO {

    private Long userId;             // 用户ID
    private String username;        // 账号
    private String name;            // 姓名
    private String phone;           // 手机号
    private String email;           // 邮箱
    private Integer age;            // 年龄
    private String gender;          // 性别
    private String address;         // 地址
    private BigDecimal balance;     // 账户余额（未开户 = 0）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
