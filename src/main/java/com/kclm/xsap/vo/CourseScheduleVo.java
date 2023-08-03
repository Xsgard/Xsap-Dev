package com.kclm.xsap.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author fangkai
 * @description
 * @create 2021-12-11 14:55
 */

@Data
@Accessors(chain = true)
public class CourseScheduleVo {

    private String title;

    private LocalDateTime start;

    private LocalDateTime end;

    private Integer height = 300;

    private String color;

    private String url;
}
