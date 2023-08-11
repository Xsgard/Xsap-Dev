package com.kclm.xsap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/2 15:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRecordDTO {
    private String courseName;

    private LocalDateTime reserveTime;

    private String cardName;

    private Integer reserveNumbers;

    private Integer timesCost;

    private LocalDateTime operateTime;

    private String operator;

    private String reserveNote;

    private Integer reserveStatus;
}
