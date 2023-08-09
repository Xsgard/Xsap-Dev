package com.kclm.xsap.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/3 19:15
 */
public class TimeUtil {
    public static LocalDateTime timeSub(LocalDateTime time, Integer day) {
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
}
