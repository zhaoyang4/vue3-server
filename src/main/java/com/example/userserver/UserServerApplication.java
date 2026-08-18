package com.example.userserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类：整个后端应用的入口。
 *
 * 要点：
 * 1. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
 *    它会自动扫描本类所在包（com.example.userserver）及其子包下的组件（@Controller/@Service/@Component 等）。
 * 2. @MapperScan 告诉 MyBatis 去哪个包找 Mapper 接口，并自动生成实现类。
 * 3. main 方法里 SpringApplication.run(...) 启动内嵌 Tomcat，默认占用 8080 端口。
 */
@SpringBootApplication
@MapperScan("com.example.userserver.mapper")
public class UserServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServerApplication.class, args);
    }
}
