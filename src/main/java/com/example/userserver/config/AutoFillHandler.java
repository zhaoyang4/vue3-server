package com.example.userserver.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器。
 *
 * 痛点：每个实体都有 create_time / update_time，每次手写 setXxxTime 很烦且易漏。
 * 解决：实体字段加 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE)，
 *       MP 在 insert/update 时自动把这两个时间填上。
 *
 * 注意：只对「标注了 fill」的字段生效；老表（user/product/order）没标注，不会受影响。
 */
@Component
public class AutoFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
