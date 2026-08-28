package com.example.userserver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态枚举 + 状态机（面试高频：订单状态如何设计、如何防止乱流转）。
 *
 * 状态：0待支付 1已支付 2已发货 3已完成 4已取消
 *
 * 合法流转（状态机）：
 *   待支付(0) → 已支付(1) / 已取消(4)
 *   已支付(1) → 已发货(2) / 已取消(4)
 *   已发货(2) → 已完成(3)
 *   已完成(3) / 已取消(4) → 终态，不可再变
 *
 * 设计要点：状态流转集中在一张表里声明，changeStatus 时先查这张表，
 * 不在集合里的流转直接拒绝（比如「待支付」不能直接跳「已完成」）。
 */
public enum OrderStatusEnum {

    PENDING(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @JsonCreator
    public static OrderStatusEnum of(int code) {
        for (OrderStatusEnum s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知订单状态: " + code);
    }

    /** 状态机：每个状态允许流转到的目标状态集合 */
    private static final Map<Integer, Set<Integer>> TRANSITIONS = new HashMap<>();

    static {
        TRANSITIONS.put(PENDING.getCode(), Set.of(PAID.getCode(), CANCELLED.getCode()));
        TRANSITIONS.put(PAID.getCode(), Set.of(SHIPPED.getCode(), CANCELLED.getCode()));
        TRANSITIONS.put(SHIPPED.getCode(), Set.of(COMPLETED.getCode()));
        TRANSITIONS.put(COMPLETED.getCode(), Collections.emptySet());
        TRANSITIONS.put(CANCELLED.getCode(), Collections.emptySet());
    }

    /** 判断 from→to 是否合法流转 */
    public static boolean canTransition(int from, int to) {
        Set<Integer> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static String descOf(int code) {
        try {
            return of(code).getDesc();
        } catch (Exception e) {
            return "未知(" + code + ")";
        }
    }
}
