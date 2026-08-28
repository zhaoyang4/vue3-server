package com.example.userserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.userserver.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 账户数据访问层。
 * 演示两种并发控制（面试高频「乐观锁 vs 悲观锁」）：
 *   - updateBalance：乐观锁，靠 version 字段防并发，不阻塞其它读，适合冲突少的场景（充值）。
 *   - selectByIdForUpdate：悲观锁，SELECT ... FOR UPDATE 直接锁住行，事务提交才释放，
 *     期间别人改不了，适合冲突多、必须强一致的场景（支付扣款）。
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 乐观锁余额变动：
     *   UPDATE account SET balance = balance + #{delta}, version = version + 1
     *   WHERE id = #{id} AND version = #{version} AND deleted = 0
     * delta 可正（充值）可负（支付）。version 不匹配（并发被改过）→ 影响行数 0 → 上层抛异常重试。
     */
    @Update("UPDATE account SET balance = balance + #{delta}, version = version + 1, " +
            "update_time = NOW() WHERE id = #{id} AND version = #{version} AND deleted = 0")
    int updateBalance(@Param("id") Long id,
                      @Param("delta") BigDecimal delta,
                      @Param("version") Integer version);

    /**
     * 悲观锁：锁住账户行。必须在 @Transactional 内调用，事务结束（commit/rollback）才释放锁。
     * 典型用法：selectByIdForUpdate 拿到行 → 校验余额 → 再 UPDATE，期间无他人能改这行。
     */
    @Select("SELECT * FROM account WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Account selectByIdForUpdate(@Param("id") Long id);
}
