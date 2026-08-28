package com.example.userserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.common.Result;
import com.example.userserver.dto.PayDTO;
import com.example.userserver.dto.RechargeDTO;
import com.example.userserver.entity.Account;
import com.example.userserver.entity.AccountFlow;
import com.example.userserver.mapper.AccountFlowMapper;
import com.example.userserver.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 账户接口。
 *   POST /api/account/recharge      充值（双写 + 乐观锁）
 *   POST /api/account/pay           余额支付（幂等 + 悲观锁）
 *   GET  /api/account/{userId}      查余额
 *   GET  /api/account/{userId}/flows 资金流水列表（双写留痕，对账用）
 *
 * @Valid 开启 DTO 上的 Bean Validation 校验；校验失败由 GlobalExceptionHandler 兜底。
 */
@RestController
@RequestMapping("/api/account")
@CrossOrigin
public class AccountController {

    @Resource
    private AccountService accountService;

    @Resource
    private AccountFlowMapper accountFlowMapper;

    /** 充值 */
    @PostMapping("/recharge")
    public Result<AccountFlow> recharge(@Valid @RequestBody RechargeDTO dto) {
        try {
            return Result.success(accountService.recharge(dto));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 余额支付（幂等，biz_no=订单号） */
    @PostMapping("/pay")
    public Result<AccountFlow> pay(@Valid @RequestBody PayDTO dto) {
        try {
            return Result.success(accountService.pay(dto));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 查询账户余额（不存在会自动开户） */
    @GetMapping("/{userId}")
    public Result<Account> info(@PathVariable Long userId) {
        return Result.success(accountService.getAccount(userId));
    }

    /** 资金流水列表（按用户倒序，分页） */
    @GetMapping("/{userId}/flows")
    public Result<IPage<AccountFlow>> flows(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<AccountFlow> page = new Page<>(current, size);
        LambdaQueryWrapper<AccountFlow> q = new LambdaQueryWrapper<>();
        q.eq(AccountFlow::getUserId, userId).orderByDesc(AccountFlow::getId);
        return Result.success(accountFlowMapper.selectPage(page, q));
    }
}
