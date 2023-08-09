package com.kclm.xsap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: 预约记录Dto
 * @date 2023/8/8 19:47
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservedInfoDto {
    private Long reserveId;
    private String memberName;
    private String phone;
    private String cardName;
    private Integer reserveNumbers;
    private Integer timesCost;
    private LocalDateTime operateTime;
    private String operator;
    private String reserveNote;
    private Integer reserveStatus;
}
