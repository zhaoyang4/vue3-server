package com.example.userserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.userserver.common.Result;
import com.example.userserver.dto.BankCardDTO;
import com.example.userserver.entity.BankCard;
import com.example.userserver.mapper.BankCardMapper;
import com.example.userserver.util.AESUtil;
import com.example.userserver.util.DesensitizeUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 银行卡接口（敏感信息加密 + 查询脱敏 演示）。
 *   POST /api/bank-card       新增（加密存储，绝不存明文）
 *   GET  /api/bank-card?userId= 列表（只返回脱敏 mask，库里是密文）
 */
@RestController
@RequestMapping("/api/bank-card")
@CrossOrigin
public class BankCardController {

    @Resource
    private BankCardMapper bankCardMapper;

    /** 新增银行卡：入库前先 AES 加密敏感字段，并计算好脱敏展示串 */
    @PostMapping
    public Result<BankCard> create(@Valid @RequestBody BankCardDTO dto) {
        BankCard card = new BankCard();
        card.setUserId(dto.getUserId());
        card.setBankName(dto.getBankName());

        // —— 加密存储：明文不落库 ——
        card.setCardNo(AESUtil.encrypt(dto.getCardNo()));
        card.setHolderName(AESUtil.encrypt(dto.getHolderName()));
        card.setPhone(AESUtil.encrypt(dto.getPhone()));

        // —— 脱敏展示串：无需解密即可展示 ——
        card.setCardNoMask(DesensitizeUtil.bankCard(dto.getCardNo()));
        card.setHolderNameMask(DesensitizeUtil.name(dto.getHolderName()));
        card.setPhoneMask(DesensitizeUtil.phone(dto.getPhone()));

        card.setDeleted(0);
        bankCardMapper.insert(card);
        return Result.success(card);
    }

    /** 列表查询：返回的是脱敏后的 mask 字段，卡号/手机号/姓名都是星号遮挡 */
    @GetMapping
    public Result<IPage<BankCard>> list(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<BankCard> page = new Page<>(current, size);
        LambdaQueryWrapper<BankCard> q = new LambdaQueryWrapper<>();
        q.eq(BankCard::getUserId, userId).orderByDesc(BankCard::getId);
        return Result.success(bankCardMapper.selectPage(page, q));
    }
}
