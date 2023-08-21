package com.kclm.xsap.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * @author Asgard
 * @version 1.0
 * @description: 时间计算工具类
 * @date 2023/8/3 19:15
 */
public class TimeUtil {
    public static LocalDateTime timeSubDays(LocalDateTime time, Integer day) {
        return time.plusDays(day.longValue());
    }

    public static LocalDateTime timeMinusDay(LocalDateTime time, Integer day) {
        return time.minusDays(day);
    }

    public static LocalDateTime timeMinusHour(LocalDateTime time, Integer hour) {
        return time.minusHours(hour);
    }

    public static LocalDateTime timeTransfer(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time);
    }

    public static LocalDateTime toEndTime(LocalDateTime start, Long minutes) {
        return start.plusMinutes(minutes);
    }

    public static Integer calculateMonths(LocalDateTime start, LocalDateTime end) {
        return Math.toIntExact(ChronoUnit.MONTHS.between(start, end));
    }
}
