package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水实体，对应 `account_flow` 表。
 *
 * 设计要点（金融高频）：
 * - 每一笔余额变动（充值/支付/退款）都必须留一条流水，是「账户余额」的可追溯凭证，对账靠它。
 * - flow_no 全局唯一（系统生成）；biz_no 是业务单号（如订单号），唯一约束是【幂等】的硬防线：
 *   同一个订单重复发起支付，biz_no 唯一约束会让第二次落库直接失败，绝不会扣两次。
 * - balance_after 记录变动后余额，出问题能直接看出某笔之后账户应是多少。
 */
@Data
@TableName("account_flow")
public class AccountFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String flowNo;        // 流水号（系统生成）
    private String bizNo;         // 业务单号（如订单号；唯一 → 幂等）
    private Long accountId;       // 账户ID
    private Long userId;          // 用户ID
    private Integer flowType;     // 流水类型 code（AccountFlowType）
    private BigDecimal amount;    // 变动金额（正数）
    private BigDecimal balanceAfter; // 变动后余额
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
