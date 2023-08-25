package com.kclm.xsap.dto;

import lombok.Data;

/**
 * @author Asgard
 * @version 1.0
 * @description: 导出预约记录Dto
 * @date 2023/8/23 15:54
 */
@Data
public class ExportReservationDTO {
    private String courseName;

    private String reserveTime;

    private String memberName;

    private String cardName;

    private Integer reserveNumbers;

    private Integer timesCost;

    private String operateTime;

    private String operator;

    private String reserveNote;

    private String reserveStatus;
}
