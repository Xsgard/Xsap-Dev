package com.kclm.xsap.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: ScheduleDetailsDto
 * @date 2023/8/7 19:17
 */
@Data
@Accessors(chain = true)
public class ScheduleDetailsDto {

    private String courseName;

    //课程开始精确时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    //课程结束精确时间
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private Long duration;
    /**
     * 限制性别
     */
    private String limitSex;
    /**
     * 限制年龄
     */
    private Integer limitAge;

    private List<String> supportCards;

    private String TeacherName;
    /**
     * 此项排课的预约人数
     */
    private Integer orderNums;

    //classNumbers??
    private Integer classNumbers;

    /**
     * 每节课程需花费的次数
     */
    private Integer timesCost;
}
