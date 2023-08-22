package com.kclm.xsap.service;

import com.kclm.xsap.vo.MemberCardStatisticsWithTotalDataInfoVo;
import com.kclm.xsap.vo.indexStatistics.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.statistics.ClassCostVo;
import com.kclm.xsap.vo.statistics.StatisticsOfCardCostVo;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: StatisticsService
 * @date 2023/8/14 19:49
 */
public interface StatisticsService {
    int MONTH = 1;
    int SEASON = 2;

    MemberCardStatisticsWithTotalDataInfoVo getCardStatisticsVo();

    List<Integer> getYearList();

    IndexAddAndStreamInfoVo cardCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo);

    ClassCostVo classCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo);

    IndexAddAndStreamInfoVo classCountMonthOrSeasonOrYear(StatisticsOfCardCostVo vo);

    IndexAddAndStreamInfoVo addAndStreamCountMonthOrSeasonOrYear(StatisticsOfCardCostVo vo);
}
