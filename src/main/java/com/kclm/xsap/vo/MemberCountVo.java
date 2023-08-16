package com.kclm.xsap.vo;

import lombok.Data;

/**
 * @author Asgard
 * @version 1.0
 * @description: 存储会员新增、流失数据
 * @date 2023/8/16 14:08
 */
@Data
public class MemberCountVo {
    private Integer newNum;

    private Integer streamNum;
}
