package com.example.userserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增银行卡入参。卡号/姓名/手机号都是敏感信息，
 * 进到 Controller 后先用 AESUtil 加密再存，绝不把明文落库。
 */
@Data
public class BankCardDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "银行名称不能为空")
    private String bankName;

    @NotBlank(message = "卡号不能为空")
    private String cardNo;

    @NotBlank(message = "持卡人不能为空")
    private String holderName;

    @NotBlank(message = "手机号不能为空")
    private String phone;
}
