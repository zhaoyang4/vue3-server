package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 银行卡实体，对应 `bank_card` 表。
 *
 * 设计要点（金融高频·敏感信息保护）：
 * - card_no / holder_name / phone 三个敏感字段入库前用 AES 加密，库里只存密文，明文不落库。
 * - card_no_mask / holder_name_mask / phone_mask 是脱敏展示串（如 6222 **** **** 1234），
 *   查询返回它们即可，前端/日志永远看不到完整明文。
 * - 真正需要明文时（如扣款时送支付渠道）再用 AESUtil.decrypt 解密，且不应回传前端。
 */
@Data
@TableName("bank_card")
public class BankCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String bankName;      // 银行名称（明文）

    private String cardNo;        // 卡号（AES 密文）
    private String cardNoMask;    // 卡号脱敏展示
    private String holderName;    // 持卡人（AES 密文）
    private String holderNameMask;// 姓名脱敏展示
    private String phone;         // 手机号（AES 密文）
    private String phoneMask;     // 手机号脱敏展示

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
