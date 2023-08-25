package com.kclm.xsap.web.controller;

import com.kclm.xsap.service.StatisticsService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.vo.MemberCardStatisticsWithTotalDataInfoVo;
import com.kclm.xsap.vo.index.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.statistics.ClassCostVo;
import com.kclm.xsap.vo.statistics.StatisticsOfCardCostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理 /statistics 请求
 * @date 2023/8/14 17:27
 */
@RequestMapping("/statistics")
@Controller
public class StatisticsController {
    private StatisticsService statisticsService;

    @Autowired
    public void setService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * @return 数据统计--会员卡统计页面
     */
    @GetMapping("/x_card_list_stat.html")
    public String toCardListStatPage() {
        return "statistics/x_card_list_stat";
    }

    /**
     * 收费统计
     *
     * @return 数据统计--收费统计页面
     */
    @GetMapping("/x_card_cost_stat.html")
    public String toCardCostStatPage() {
        return "statistics/x_card_cost_stat";
    }

    /**
     * 课消统计
     *
     * @return 数据统计--课消统计页面
     */
    @GetMapping("/x_class_cost_stat.html")
    public String toClassCostStatPage() {
        return "statistics/x_class_cost_stat";
    }

    /**
     * 总课次统计
     *
     * @return 数据统计--总课次统计页面
     */
    @GetMapping("/x_class_hour_stat.html")
    public String toClassHourStatPage() {
        return "statistics/x_class_hour_stat";
    }

    /**
     * 新增与流失统计
     *
     * @return 数据统计--新增与流失统计页面
     */
    @GetMapping("/x_member_num_static.html")
    public String toMemberNumStaticPage() {
        return "statistics/x_member_num_static";
    }

    @PostMapping("/cardInfo.do")
    @ResponseBody
    public R getCardInfo() {
        MemberCardStatisticsWithTotalDataInfoVo cardStatisticsVo = statisticsService.getCardStatisticsVo();
        return R.ok().put("data", cardStatisticsVo);
    }

    @PostMapping("/yearList")
    @ResponseBody
    public R getYearList() {
        List<Integer> yearList = statisticsService.getYearList();
        return R.ok().put("data", yearList);
    }

    @PostMapping("/cardCostMonthOrSeasonOrYear")
    @ResponseBody
    public R cardCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        IndexAddAndStreamInfoVo indexAddAndStreamInfoVo = statisticsService.cardCostMonthOrSeasonOrYear(vo);
        return R.ok().put("data", indexAddAndStreamInfoVo);
    }

    @PostMapping("/classCostMonthOrSeasonOrYear")
    @ResponseBody
    public R classCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        ClassCostVo classCostVo = statisticsService.classCostMonthOrSeasonOrYear(vo);
        return R.ok().put("data", classCostVo);
    }

    @PostMapping("/classCountMonthOrSeasonOrYear")
    @ResponseBody
    public R classCountMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        IndexAddAndStreamInfoVo indexAddAndStreamInfoVo = statisticsService.classCountMonthOrSeasonOrYear(vo);
        return R.ok().put("data", indexAddAndStreamInfoVo);
    }

    @PostMapping("/addAndStreamCountMonthOrSeasonOrYear")
    @ResponseBody
    public R addAndStreamCountMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        IndexAddAndStreamInfoVo indexAddAndStreamInfoVo = statisticsService.addAndStreamCountMonthOrSeasonOrYear(vo);
        return R.ok().put("data", indexAddAndStreamInfoVo);
    }
}
