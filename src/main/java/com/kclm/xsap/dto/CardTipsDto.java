package com.kclm.xsap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Asgard
 * @version 1.0
 * @description: 用于传输会员卡可用次数及课程收取次数的实体
 * @date 2023/8/8 13:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardTipsDto {
    private Integer cardTotalCount;
    private Integer courseTimesCost;
}
