package com.example.userserver.util;

/**
 * 脱敏工具（演示「查询脱敏」）。
 *
 * 原则：敏感字段入库前加密（见 AESUtil），查出来展示时再脱敏，
 * 前端/日志永远看不到完整明文（如完整卡号、手机号）。
 */
public class DesensitizeUtil {

    /** 手机号：13812348888 → 138****8888 */
    public static String phone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /** 银行卡：6222021234567890 → 6222 **** **** 7890（保留前4后4，中间按4位分段） */
    public static String bankCard(String cardNo) {
        if (cardNo == null || cardNo.length() < 8) {
            return cardNo;
        }
        String clean = cardNo.replaceAll("\\s+", "");
        String head = clean.substring(0, 4);
        String tail = clean.substring(clean.length() - 4);
        return head + " **** **** " + tail;
    }

    /** 姓名：张三丰 → 张*；欧阳娜娜 → 欧** */
    public static String name(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
}
