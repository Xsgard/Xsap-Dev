package com.kclm.xsap.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author fangkai
 * @description
 * @create 2021-12-22 10:06
 */
@Data
@Accessors(chain = true)
public class ConsumeFormVo {
    //上课id
    private Long classId;
    //会员id
    private Long memberId;
    //卡绑定id
    private Long cardBindId;
    //操作人
    private String operator;
    //排课记录id
    private Long scheduleId;
    //自定义扣费时的消费金额
    private BigDecimal amountOfConsumption;
    /**
     * 扣除次数
     */
    @NotNull(message = "请输入扣除次数")
    private Integer cardCountChange;

    private Integer cardDayChange;

    private String note;

}
