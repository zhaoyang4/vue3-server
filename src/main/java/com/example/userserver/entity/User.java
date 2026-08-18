package com.example.userserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，与数据库表 `user` 一一对应。
 *
 * 注解说明：
 * - @TableName("user")：指定对应哪张表（表名和类名不一致时必写）。
 * - @TableId(type = IdType.AUTO)：主键，AUTO 表示由数据库自增。
 * - @Data（Lombok）：自动生成 getter/setter/equals/hashCode/toString。
 *
 * 字段命名：Java 用驼峰（createTime），数据库用下划线（create_time）。
 * MyBatis-Plus 默认开启"驼峰转下划线"映射，所以会自动对应，无需额外配置。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;            // 主键

    private String username;    // 登录账号（唯一）
    private String name;        // 姓名
    private String phone;       // 手机号
    private String email;       // 邮箱
    private Integer age;        // 年龄
    private String gender;      // 性别（男/女/其他）
    private String address;     // 地址

    // @JsonFormat：指定序列化成 "yyyy-MM-dd HH:mm:ss" 并按东八区展示，
    // 避免默认输出带 T 和毫秒的 ISO 格式（如 2026-08-17T14:36:40.7342886）。
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;  // 创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;  // 更新时间
}
