package com.example.userserver.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 启动前的「环境变量体检」。
 *
 * <p><b>为什么需要它？</b>
 * 生产配置里写的是 ${DB_HOST} 这类占位符。如果服务器上忘了设环境变量，
 * Spring 并不会明确报"变量缺失"，而是把 ${DB_HOST} 原样当成主机名去连数据库，
 * 于是日志里冒出一句让人一脸问号的 <code>UnknownHostException: ${DB_HOST}</code>。
 *
 * <p>所以在 Spring 启动之前先自己查一遍：缺什么就直接告诉你缺什么，然后立刻退出。
 * 这叫 fail-fast —— 启动阶段暴露问题，比带着错配置跑起来、半夜再炸强得多。
 *
 * <p>只在 prod / sit 生效；本地 dev 全都有默认值，不打扰开发。
 */
public final class EnvGuard {

    private EnvGuard() {
    }

    /** 生产环境必须提供的环境变量 */
    private static final List<String> REQUIRED_PROD = List.of(
            "DB_HOST",
            "DB_NAME",
            "DB_USERNAME",
            "DB_PASSWORD",
            "JWT_SECRET",
            "CORS_ALLOWED_ORIGINS"
    );

    /** 测试环境必须提供的环境变量（地址和密码不能靠默认值） */
    private static final List<String> REQUIRED_SIT = List.of(
            "DB_HOST",
            "DB_PASSWORD"
    );

    /**
     * 校验入口。放在 main() 里 SpringApplication.run 之前调用。
     *
     * @param args 命令行参数（用于识别 --spring.profiles.active=xxx）
     */
    public static void check(String[] args) {
        String profile = resolveActiveProfile(args);

        List<String> required;
        if (profile.contains("prod")) {
            required = REQUIRED_PROD;
        } else if (profile.contains("sit")) {
            required = REQUIRED_SIT;
        } else {
            // dev 或未指定：本地开发，配置文件里都有默认值，不做强制检查
            return;
        }

        Map<String, String> env = System.getenv();
        List<String> missing = new ArrayList<>();
        for (String key : required) {
            String value = env.get(key);
            if (value == null || value.isBlank()) {
                missing.add(key);
            }
        }

        if (!missing.isEmpty()) {
            System.err.println();
            System.err.println("============================================================");
            System.err.println(" 启动中止：profile=" + profile + " 缺少必需的环境变量");
            System.err.println("------------------------------------------------------------");
            for (String key : missing) {
                System.err.println("   缺少 -> " + key);
            }
            System.err.println("------------------------------------------------------------");
            System.err.println(" 怎么修：");
            System.err.println("   Docker    : 在 .env 里补齐后 docker compose up -d");
            System.err.println("   systemd   : 写进 /etc/vue3-server.env 后 systemctl restart vue3-server");
            System.err.println("   手动启动  : export DB_HOST=... 或用 deploy/env.sh");
            System.err.println(" 完整清单见 DEPLOY.md 第五节「环境变量清单」");
            System.err.println("============================================================");
            System.err.println();
            // 退出码 1：systemd / docker 能识别为启动失败并按策略重试
            System.exit(1);
        }

        // JWT 密钥强度提醒：太短的密钥等于没有安全性
        String jwt = env.get("JWT_SECRET");
        if (jwt != null && jwt.length() < 32) {
            System.err.println("[警告] JWT_SECRET 长度不足 32 位，安全性很弱，建议用 openssl rand -base64 48 重新生成");
        }
    }

    /**
     * 找出当前激活的 profile，优先级：命令行参数 > 环境变量 > 默认 dev。
     * 命令行支持两种写法：--spring.profiles.active=prod / -Dspring.profiles.active=prod
     */
    private static String resolveActiveProfile(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("--spring.profiles.active=")) {
                    return arg.substring(arg.indexOf('=') + 1).trim().toLowerCase();
                }
            }
        }
        String fromJvm = System.getProperty("spring.profiles.active");
        if (fromJvm != null && !fromJvm.isBlank()) {
            return fromJvm.trim().toLowerCase();
        }
        String fromEnv = System.getenv("SPRING_PROFILES_ACTIVE");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim().toLowerCase();
        }
        return "dev";
    }
}
