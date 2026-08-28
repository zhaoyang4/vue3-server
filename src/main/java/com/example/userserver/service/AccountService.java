package com.example.userserver.service;

import com.example.userserver.dto.PayDTO;
import com.example.userserver.dto.RechargeDTO;
import com.example.userserver.entity.Account;
import com.example.userserver.entity.AccountFlow;

/**
 * 资金账户服务（金融/电商高频场景）。
 */
public interface AccountService {

    /**
     * 充值：账户余额 + 资金流水【双写】在同一事务，乐观锁防并发亏钱。
     * 双写一致性是核心：任一步失败整体回滚，绝不会出现「余额加了但没流水」或反之。
     */
    AccountFlow recharge(RechargeDTO dto);

    /**
     * 余额支付：幂等（biz_no 唯一约束兜底）+ 悲观锁（SELECT ... FOR UPDATE）扣款。
     * 同一订单号重复调用，第二次直接返回已有流水，绝不重复扣钱。
     */
    AccountFlow pay(PayDTO dto);

    /** 查询账户（不存在则自动开户） */
    Account getAccount(Long userId);
}
