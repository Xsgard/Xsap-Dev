package com.kclm.xsap.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author Asgard
 * @version 1.0
 * @description: 传递银行卡绑定信息
 * @date 2023/8/5 9:34
 */
@Data
public class BindCardInfoDto {
    private Long memberId;

    private String operator;

    @NotBlank(message = "请选择一张卡")
    private Long cardId;

    @NotBlank(message = "请输入充值次数")
    private Integer validCount;

    @NotBlank(message = "请输入有效期")
    private Integer validDay;

    @NotBlank(message = "请输入实收金额")
    private Integer receivedMoney;

    @NotBlank(message = "请选择支付方式")
    private String payMode;

    private String note;
}
