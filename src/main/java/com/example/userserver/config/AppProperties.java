package com.example.userserver.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务自定义配置的「接收器」。
 *
 * <p>作用：把 application.yml 里 app.* 开头的配置，自动装进这个 Java 对象。
 * 之后任何地方需要用，直接注入 AppProperties 即可，不用满项目写 @Value("${...}")。
 *
 * <p>对应关系（yml -> 字段，中划线自动转驼峰）：
 * <pre>
 * app:
 *   cors:
 *     allowed-origins: http://a.com,http://b.com   ->  cors.allowedOrigins = [http://a.com, http://b.com]
 *   jwt:
 *     secret: xxx                                  ->  jwt.secret
 *     expire-minutes: 120                          ->  jwt.expireMinutes
 * </pre>
 *
 * <p>前端类比：相当于把散落各处的 import.meta.env.VITE_XXX 收拢成一个 config 对象再导出。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** 跨域相关配置 */
    private Cors cors = new Cors();

    /** 令牌（JWT）相关配置 */
    private Jwt jwt = new Jwt();

    @Data
    public static class Cors {
        /**
         * 允许跨域的前端来源，yml 里用逗号分隔即可自动转成 List。
         * 注意：必须写具体来源（协议+域名+端口），不能用 *，否则浏览器不允许携带 Cookie。
         */
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }

    @Data
    public static class Jwt {
        /**
         * 令牌签名密钥。生产环境必须通过环境变量 JWT_SECRET 注入。
         * 目前项目还没接入登录鉴权，这里先把配置通道打通：
         * 以后写 JwtUtil 时直接注入 AppProperties 拿 secret，不需要再改配置结构。
         */
        private String secret = "dev-only-insecure-secret-please-change-in-prod";

        /** 令牌有效期（分钟） */
        private long expireMinutes = 120;
    }
}
