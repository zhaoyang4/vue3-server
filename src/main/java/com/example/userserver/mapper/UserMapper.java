package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.userserver.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据访问层（DAO）。
 *
 * 只需继承 BaseMapper<User>，MyBatis-Plus 就自动提供了全套 CRUD 方法：
 *   - insert / deleteById / updateById / selectById
 *   - selectList / selectPage / selectCount ...
 * 绝大多数场景不用自己写 SQL。只有复杂查询才需要额外写。
 *
 * @Mapper 让 Spring 能扫描并创建这个接口的代理实现。
 * （启动类上的 @MapperScan 已经统一扫描，这里写不写 @Mapper 都行，写上更直观。）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础 CRUD 已由 BaseMapper 提供，无需任何代码
}
