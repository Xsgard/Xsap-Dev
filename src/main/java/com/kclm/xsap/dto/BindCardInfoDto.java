package com.kclm.xsap.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Asgard
 * @version 1.0
 * @description: 传递银行卡绑定信息
 * @date 2023/8/5 9:34
 */
@Data
public class BindCardInfoDto {
    @NotNull(message = "请选择一个会员！")
    private Long memberId;

    private String operator;

    @NotNull(message = "请选择一张卡")
    private Long cardId;

    @NotNull(message = "请输入充值次数")
    @Min(value = 0, message = "输入有误，请检查输入次数！")
    private Integer validCount;

    @NotNull(message = "请输入有效期")
    @Min(value = 0, message = "输入有误，请检查输入天数！")
    private Integer validDay;

    @NotNull(message = "请输入实收金额")
    @Min(value = 0, message = "输入有误，请检查输入金额！")
    private Integer receivedMoney;

    @NotBlank(message = "请选择支付方式")
    private String payMode;

    private String note;
}
