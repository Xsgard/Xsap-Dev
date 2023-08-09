package com.kclm.xsap.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/9 14:31
 */
@Data
public class ReverseClassRecordDto {
    private Long reserveId;
    private String memberName;
    private String memberPhone;
    private String cardName;
    private String memberSex;
    private LocalDate memberBirthday;
    private Integer timesCost;
    private Integer reserveNums;
    private LocalDateTime operateTime;
    private Integer checkStatus;
}
