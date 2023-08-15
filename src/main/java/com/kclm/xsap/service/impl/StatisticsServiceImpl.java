package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.dao.RechargeRecordDao;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.vo.MemberCardStatisticsVo;
import com.kclm.xsap.vo.MemberCardStatisticsWithTotalDataInfoVo;
import com.kclm.xsap.vo.indexStatistics.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.statistics.StatisticsOfCardCostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Asgard
 * @version 1.0
 * @description: StatisticsServiceImpl
 * @date 2023/8/14 19:50
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    //
    private static final LocalTime startTime = LocalTime.of(0, 0, 0);
    //
    private static final LocalTime endTime = LocalTime.of(23, 59, 59);


    private MemberService memberService;
    private MemberBindRecordService memberBindRecordService;
    private MemberCardService memberCardService;
    private ConsumeRecordService consumeRecordService;
    private RechargeRecordService rechargeRecordService;
    private RechargeRecordDao rechargeRecordDao;

    @Autowired
    public void setDao(RechargeRecordDao rechargeRecordDao) {
        this.rechargeRecordDao = rechargeRecordDao;
    }

    @Autowired
    public void setMemberBindRecordService(MemberBindRecordService memberBindRecordService, ConsumeRecordService consumeRecordService,
                                           MemberService memberService, MemberCardService memberCardService,
                                           RechargeRecordService rechargeRecordService) {
        this.rechargeRecordService = rechargeRecordService;
        this.memberCardService = memberCardService;
        this.consumeRecordService = consumeRecordService;
        this.memberService = memberService;
        this.memberBindRecordService = memberBindRecordService;
    }

    /**
     * 会员卡统计
     *
     * @return List
     */
    @Override
    public MemberCardStatisticsWithTotalDataInfoVo getCardStatisticsVo() {
        List<MemberEntity> members = memberService.list();
        List<MemberCardStatisticsVo> memberCardStatisticsVos = new ArrayList<>();
        members.forEach(item -> {
            //查询绑定记录
            LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MemberBindRecordEntity::getMemberId, item.getId());
            //
            List<MemberBindRecordEntity> bindRecords = memberBindRecordService.list(queryWrapper);
            bindRecords.forEach(e -> {
                //VO
                MemberCardStatisticsVo vo = new MemberCardStatisticsVo();
                //已使用课次
                Integer usedClassCost = consumeRecordService.queryUsedClassCost(e.getId());
                //已使用金额
                BigDecimal money = consumeRecordService.queryAmountPayable(e.getId());
                //
                vo.setMemberId(item.getId());
                vo.setMemberName(item.getName());
                vo.setBindCardId(e.getCardId());
                //设置总次数 -- 已使用次数加绑定表中可使用次数
                vo.setTotalClassTimes(usedClassCost + e.getValidCount());
                //已使用课次
                vo.setUsedClassTimes(usedClassCost);
                //剩余课次
                vo.setRemainingClassTimes(e.getValidCount());
                //总金额
                vo.setLumpSumBigD(e.getReceivedMoney());
                vo.setLumpSum(e.getReceivedMoney().toString());
                //已用金额
                vo.setAmountUsedBigD(money);
                vo.setAmountUsed(money.toString());
                //剩余金额
                vo.setBalanceBigD(e.getReceivedMoney().subtract(money));
                vo.setBalance(e.getReceivedMoney().subtract(money).toString());

                memberCardStatisticsVos.add(vo);
            });
        });
        //
        MemberCardStatisticsWithTotalDataInfoVo infoVo = new MemberCardStatisticsWithTotalDataInfoVo();
        //总课时
        Integer totalCourseTimeAll = memberCardStatisticsVos.stream()
                .mapToInt(MemberCardStatisticsVo::getTotalClassTimes).sum();
        infoVo.setTotalCourseTimeAll(totalCourseTimeAll);
        //已使用课时
        Integer usedCourseTimeAll = memberCardStatisticsVos.stream()
                .mapToInt(MemberCardStatisticsVo::getUsedClassTimes).sum();
        infoVo.setUsedCourseTimeAll(usedCourseTimeAll);
        //剩余课时
        Integer remainCourseTimeAll = memberCardStatisticsVos.stream()
                .mapToInt(MemberCardStatisticsVo::getRemainingClassTimes).sum();
        infoVo.setRemainCourseTimeAll(remainCourseTimeAll);
        //总金额
        BigDecimal totalMoneyAll = BigDecimal.valueOf(memberCardStatisticsVos.stream()
                .mapToDouble(e -> e.getLumpSumBigD().doubleValue()).sum());
        infoVo.setTotalMoneyAll(totalMoneyAll);
        //已用金额
        BigDecimal usedMoneyAll = BigDecimal.valueOf(memberCardStatisticsVos.stream()
                .mapToDouble(i -> i.getAmountUsedBigD().doubleValue()).sum());
        infoVo.setUsedMoneyAll(usedMoneyAll);
        //剩余金额
        BigDecimal remainMoneyAll = BigDecimal.valueOf(memberCardStatisticsVos.stream()
                .mapToDouble(r -> r.getBalanceBigD().doubleValue()).sum());
        infoVo.setRemainMoneyAll(remainMoneyAll);

        infoVo.setMemberCardStatisticsVos(memberCardStatisticsVos);
        return infoVo;
    }

    /**
     * 获取年份的集合
     *
     * @return List
     */
    @Override
    public List<Integer> getYearList() {
        List<LocalDateTime> localDateTimes = rechargeRecordDao.getLocalDateTimes();
        return localDateTimes.stream()
                .map(LocalDateTime::getYear)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public IndexAddAndStreamInfoVo cardCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {

        if (vo.getUnit() == 1) {
            //统计时段 --月
            return cardCostHandler(vo.getYearOfSelect());
        } else if (vo.getUnit() == 2) {
            //统计时段 --季
            return cardCostHandler(vo.getYearOfSelect(), vo.getUnit());
        } else {
            //统计时段 --年
            return cardCostHandler(vo);
        }

    }

    /**
     * 按月份收费统计
     *
     * @param year 查找年份
     * @return IndexAddAndStreamInfoVo
     */
    private IndexAddAndStreamInfoVo cardCostHandler(Integer year) {
        IndexAddAndStreamInfoVo result = new IndexAddAndStreamInfoVo();
        result.setTitle("月收费模式");
        result.setXname("月");
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        //本年第一天
        LocalDate startDate = LocalDate.of(year, 1, 1);
        //先判断查找年份是否为本年
        LocalDateTime now = LocalDateTime.now();
        if (now.getYear() == year) {
            int month = now.getMonth().getValue();
            for (int i = 0; i < month; i++) {
                time.add((i + 1) + "月份");
            }
            data = rechargeRecordService.getRechargeList(
                    TimeUtil.timeTransfer(startDate, startTime), now);
        } else {
            for (int i = 0; i < 12; i++) {
                time.add((i + 1) + "月份");
            }
            LocalDate endDate = LocalDate.of(year, 12, 31);
            data = rechargeRecordService.getRechargeList(
                    TimeUtil.timeTransfer(startDate, startTime),
                    TimeUtil.timeTransfer(endDate, endTime));
        }
        result.setData(data);
        result.setTime(time);
        return result;
    }

    /**
     * 按季度收费统计
     *
     * @param year 查找年份
     * @param unit 标志为（季度）重载
     * @return IndexAddAndStreamInfoVo
     */
    private IndexAddAndStreamInfoVo cardCostHandler(Integer year, Integer unit) {
        IndexAddAndStreamInfoVo result = new IndexAddAndStreamInfoVo();
        result.setTitle("季度收费模式");
        result.setXname("季度");
        List<String> time;
        //
        LocalDateTime now = LocalDateTime.now();
        //
        if (now.getYear() == year) {
            int month = now.getMonthValue();

            time = new ArrayList<>();

        } else {
            time = new ArrayList<>(Arrays.asList("第1季度", "第2季度", "第3季度", "第4季度"));

        }

        result.setTime(time);
        return result;
    }

    /**
     * 按年份收费统计
     *
     * @param vo 封装的统计条件
     * @return IndexAddAndStreamInfoVo
     */
    private IndexAddAndStreamInfoVo cardCostHandler(StatisticsOfCardCostVo vo) {
        IndexAddAndStreamInfoVo result = new IndexAddAndStreamInfoVo();
        result.setTitle("年收费模式");
        result.setXname("年");

        return result;
    }
}
