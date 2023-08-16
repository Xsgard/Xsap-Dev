package com.kclm.xsap.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Asgard
 * @version 1.0
 * @description: 传输老师课消和金额统计
 * @date 2023/8/15 19:47
 */
@Data
public class TeacherConsumeVo {
    private String teacherName;

    private BigDecimal moneyCost;

    private Integer countChange;
}
