package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金账户实体，对应 `account` 表。
 *
 * 设计要点（金融高频）：
 * - balance 用 BigDecimal（DECIMAL），禁用 double，否则会出现 0.1+0.2≠0.3 的精度事故。
 * - version 乐观锁字段：余额变动时 WHERE version=?，被并发改过就更新失败（见 AccountMapper.updateBalance）。
 * - deleted 逻辑删除：@TableLogic 让所有查询自动带 deleted=0，删除变更新，数据可追溯。
 * - createTime/updateTime 由 AutoFillHandler 自动填充。
 */
@Data
@TableName("account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;          // 用户ID（一个用户一个账户，uk_user_id 唯一）
    private BigDecimal balance;   // 账户余额
    private Integer version;      // 乐观锁版本号

    @TableLogic
    private Integer deleted;      // 逻辑删除 0/1

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
