package com.kclm.xsap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: 团课课程表Dto
 * @date 2023/8/7 16:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleRecordDto {
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer height;
    private String color;
    private String url;
}
