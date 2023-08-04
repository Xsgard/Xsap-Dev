package com.kclm.xsap.utils;

import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/3 19:15
 */
public class TimeCalculate {
    public static LocalDateTime timeSub(LocalDateTime time, Integer day) {
        return time.plusDays(day.longValue());
    }
}
