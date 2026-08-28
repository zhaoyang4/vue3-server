package com.example.userserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.userserver.common.BizException;
import com.example.userserver.dto.PayDTO;
import com.example.userserver.dto.RechargeDTO;
import com.example.userserver.entity.Account;
import com.example.userserver.entity.AccountFlow;
import com.example.userserver.enums.AccountFlowType;
import com.example.userserver.mapper.AccountFlowMapper;
import com.example.userserver.mapper.AccountMapper;
import com.example.userserver.service.AccountService;
import com.example.userserver.util.BizNoGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资金账户业务实现。三个核心金融知识点：
 *
 * 1) 双写一致性（recharge）：余额更新 + 流水插入在同一个 @Transactional 里，
 *    任一步异常整体回滚，保证「余额」和「流水」永远对得上。
 *
 * 2) 乐观锁（recharge 用 updateBalance 带 version）：冲突少的充值场景，不阻塞读，
 *    更新失败（影响行数 0）即视为并发冲突，提示重试。
 *
 * 3) 幂等 + 悲观锁（pay）：先查 biz_no 是否已支付（应用层幂等），
 *    再用 SELECT ... FOR UPDATE 锁行（悲观锁，强一致），最后写流水时
 *    biz_no 唯一约束是最后一道防线（DB 层幂等）。三重保障不重复扣钱。
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Resource
    private AccountFlowMapper accountFlowMapper;

    @Override
    public Account getAccount(Long userId) {
        return getOrCreate(userId);
    }

    /** 查账户，没有就开户（首次充值/支付前保证有账户） */
    private Account getOrCreate(Long userId) {
        Account acc = baseMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId));
        if (acc == null) {
            acc = new Account();
            acc.setUserId(userId);
            acc.setBalance(BigDecimal.ZERO);
            acc.setVersion(0);
            acc.setDeleted(0);
            baseMapper.insert(acc);
        }
        return acc;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountFlow recharge(RechargeDTO dto) {
        Account acc = getOrCreate(dto.getUserId());

        // 1) 乐观锁余额增加：WHERE version=?，被并发改过则影响行数 0 → 抛异常（建议上游重试）
        int rows = baseMapper.updateBalance(acc.getId(), dto.getAmount(), acc.getVersion());
        if (rows == 0) {
            throw new BizException("账户并发变更，请重试");
        }

        // 2) 写流水（与余额变动同一事务 → 双写一致性）
        Account after = baseMapper.selectById(acc.getId());
        AccountFlow flow = buildFlow(after, AccountFlowType.RECHARGE, dto.getAmount(),
                dto.getRemark() == null ? "充值" : dto.getRemark(), null);
        accountFlowMapper.insert(flow);
        return flow;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountFlow pay(PayDTO dto) {
        // 0) 幂等（应用层）：同一订单号(biz_no)的支付流水已存在 → 直接返回，绝不重复扣钱
        List<AccountFlow> exist = accountFlowMapper.selectList(
                new LambdaQueryWrapper<AccountFlow>()
                        .eq(AccountFlow::getBizNo, dto.getOrderNo())
                        .last("LIMIT 1"));
        if (!exist.isEmpty()) {
            return exist.get(0);
        }

        Account acc = getOrCreate(dto.getUserId());

        // 1) 悲观锁：锁住账户行（事务提交才释放），期间无人能改这行 → 强一致
        Account locked = baseMapper.selectByIdForUpdate(acc.getId());
        if (locked.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new BizException("账户余额不足");
        }

        // 2) 扣款（行已锁，用 version 兜底即可，不会并发冲突）
        baseMapper.updateBalance(locked.getId(), dto.getAmount().negate(), locked.getVersion());

        // 3) 写支付流水（biz_no=订单号，唯一约束是 DB 层幂等最后防线）
        Account after = baseMapper.selectById(locked.getId());
        AccountFlow flow = buildFlow(after, AccountFlowType.PAY, dto.getAmount(),
                "余额支付 订单:" + dto.getOrderNo(), dto.getOrderNo());
        accountFlowMapper.insert(flow);
        return flow;
    }

    /** 组装一条流水记录（flow_no 全局唯一，balanceAfter 记变动后余额） */
    private AccountFlow buildFlow(Account acc, AccountFlowType type, BigDecimal amount,
                                  String remark, String bizNo) {
        AccountFlow flow = new AccountFlow();
        flow.setFlowNo(BizNoGenerator.genFlowNo());
        flow.setBizNo(bizNo); // 充值传 null，支付传订单号
        flow.setAccountId(acc.getId());
        flow.setUserId(acc.getUserId());
        flow.setFlowType(type.getCode());
        flow.setAmount(amount);
        flow.setBalanceAfter(acc.getBalance());
        flow.setRemark(remark);
        return flow;
    }
}
