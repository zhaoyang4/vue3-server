package com.example.userserver.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 业务单号 / 流水号生成器。
 * 格式：前缀 + 时间(yyyyMMddHHmmss) + 纳秒后6位 + 随机3位，碰撞概率极低，无需数据库自增。
 */
public class BizNoGenerator {

    /** 生成全局唯一流水号，如 FL20260820153045123456042 */
    public static String genFlowNo() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String ts = LocalDateTime.now().format(f);
        int nano = Math.abs((int) (System.nanoTime() % 1000000));
        int rnd = (int) (Math.random() * 900 + 100);
        return "FL" + ts + String.format("%06d", nano) + rnd;
    }
}
