package com.kclm.xsap.service;

import com.kclm.xsap.vo.MemberCardStatisticsWithTotalDataInfoVo;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: StatisticsService
 * @date 2023/8/14 19:49
 */
public interface StatisticsService {
    MemberCardStatisticsWithTotalDataInfoVo getCardStatisticsVo();

    List<Integer> getYearList();
}
