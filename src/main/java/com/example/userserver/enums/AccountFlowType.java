package com.example.userserver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 资金流水类型枚举。
 * 用「code + desc」而不是裸数字，DB 里存 code（1/2/3），接口返回也是 code，
 * 前端/日志看到的是可读数字，避免 Magic Number。
 */
public enum AccountFlowType {

    RECHARGE(1, "充值"),
    PAY(2, "支付"),
    REFUND(3, "退款");

    private final int code;
    private final String desc;

    AccountFlowType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /** 序列化给前端时用 code（数字），而不是枚举名 */
    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 前端传 code 进来时，反序列化成枚举（找不到就报错，天然校验非法值） */
    @JsonCreator
    public static AccountFlowType of(int code) {
        for (AccountFlowType t : values()) {
            if (t.code == code) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知流水类型: " + code);
    }
}
