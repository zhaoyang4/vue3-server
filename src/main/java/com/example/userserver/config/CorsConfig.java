package com.example.userserver.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局跨域（CORS）配置。
 *
 * <p><b>为什么需要它？</b>
 * 浏览器同源策略：页面在 http://ip:80，接口在 http://ip:8080，域名端口不同就是「跨域」，
 * 浏览器会先发一个 OPTIONS 预检请求问后端「你允许我吗」，后端不回声明就直接拦掉。
 *
 * <p><b>为什么不用 @CrossOrigin 注解了？</b>
 * 注解写在 Controller 上属于「局部放行」，每加一个 Controller 就要记得加一遍，
 * 而且默认放行 * ，生产环境等于对全网开放。统一收到这里，放行名单由配置/环境变量决定。
 *
 * <p><b>本地和生产分别怎么走？</b>
 * <ul>
 *   <li>本地开发：Vite 的 dev proxy 已经把 /api 代理到 8080，浏览器看到的是同源，本来就不跨域；
 *       这份配置是给「前端直连后端 IP 调试」的场景兜底。</li>
 *   <li>生产推荐：Nginx 把前端静态页和 /api 反代放在同一个域名下 -> 同源，CORS 根本不会触发（最省事最安全）。</li>
 *   <li>生产直连：前端 build 时把 VITE_API_BASE 写成 http://后端IP:8080，就必须靠这里放行前端域名。</li>
 * </ul>
 *
 * <p>前端类比：等同于 vite.config.js 里 server.proxy 的"另一半"——proxy 是前端绕过跨域，CORS 是后端正式授权。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = appProperties.getCors().getAllowedOrigins().toArray(new String[0]);
        log.info("[CORS] 已放行的前端来源: {}", String.join(", ", origins));

        registry.addMapping("/api/**")
                // 放行名单来自配置，可被环境变量 CORS_ALLOWED_ORIGINS 覆盖
                // 用 allowedOriginPatterns 而非 allowedOrigins：支持 http://192.168.1.* 这类通配写法
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // 允许浏览器携带凭证（Cookie / Authorization 头），将来接 JWT 要用
                .allowCredentials(true)
                // 预检结果缓存 1 小时，减少 OPTIONS 请求次数
                .maxAge(3600);
    }
}
