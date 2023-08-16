package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.dao.RechargeRecordDao;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.vo.MemberCardStatisticsVo;
import com.kclm.xsap.vo.MemberCardStatisticsWithTotalDataInfoVo;
import com.kclm.xsap.vo.TeacherConsumeVo;
import com.kclm.xsap.vo.TempList;
import com.kclm.xsap.vo.indexStatistics.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.statistics.ClassCostVo;
import com.kclm.xsap.vo.statistics.StatisticsOfCardCostVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    private EmployeeService employeeService;
    private ScheduleRecordService scheduleRecordService;

    @Autowired
    public void setDao(RechargeRecordDao rechargeRecordDao) {
        this.rechargeRecordDao = rechargeRecordDao;
    }

    @Autowired
    public void setMemberBindRecordService(MemberBindRecordService memberBindRecordService, ConsumeRecordService consumeRecordService,
                                           MemberService memberService, MemberCardService memberCardService,
                                           RechargeRecordService rechargeRecordService,
                                           EmployeeService employeeService,
                                           ScheduleRecordService scheduleRecordService) {
        this.scheduleRecordService = scheduleRecordService;
        this.employeeService = employeeService;
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
     * 老师课时消费月统计
     *
     * @param vo 封装的查询年份等
     * @return IndexAddAndStreamInfoVo
     */
    @Override
    public ClassCostVo classCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        if (vo.getUnit() == 1) {
            return classCostHandler(vo.getYearOfSelect());
        } else if (vo.getUnit() == 2) {
            return classCostHandler(vo.getYearOfSelect(), vo.getUnit());
        } else {
            return classCostHandler(vo);
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
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        for (int i = 1; i <= 4; i++) {
            time.add("第" + i + "季度");
            LocalDateTime endDateTime = startDateTime.plusMonths(3);
            data.add(rechargeRecordService.getRechargeList(startDateTime, endDateTime).get(0));
            startDateTime = startDateTime.plusMonths(3);
            if (endDateTime.isAfter(now))
                break;
        }
        result.setTime(time);
        result.setData(data);
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
        List<Integer> data = new ArrayList<>();
        List<String> time = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(vo.getBeginYear(), 1, 1, 0, 0, 0);
        for (int i = vo.getBeginYear(); i <= vo.getEndYear(); i++) {
            time.add(String.valueOf(i));
            LocalDateTime end = start.plusYears(1);
            data.add(rechargeRecordService.getRechargeList(start, end).get(0));
            start = start.plusYears(1);
        }
        result.setTime(time);
        result.setData(data);

        return result;
    }


    /**
     * 按月份课消统计
     *
     * @param year 查找年份
     * @return IndexAddAndStreamInfoVo
     */
    private ClassCostVo classCostHandler(Integer year) {
        ClassCostVo vo = new ClassCostVo();
        vo.setTitle("老师课时消费月统计");
        vo.setXname("月");
        List<List<Integer>> data = new ArrayList<>();
        List<List<Float>> data2 = new ArrayList<>();
        List<String> tName = new ArrayList<>();
        List<String> time = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        Map<String, TempList> dataMap = new HashMap<>();
        for (int i = 0; i < 12; i++) {
            time.add((i + 1) + "月");
            LocalDateTime endTime = startDateTime.plusMonths(1);
            teacherConsumeVoHandler(startDateTime, dataMap, endTime);
            startDateTime = startDateTime.plusMonths(1);
            if (startDateTime.isAfter(now))
                break;
        }
        return getClassCostVo(vo, data, data2, tName, time, dataMap);
    }

    /**
     * 按季度课消统计
     *
     * @param year 查找年份
     * @param unit 标志为（季度）重载
     * @return IndexAddAndStreamInfoVo
     */
    private ClassCostVo classCostHandler(Integer year, Integer unit) {
        ClassCostVo vo = new ClassCostVo();
        vo.setTitle("老师课时消费季度统计");
        vo.setXname("季度");
        List<List<Integer>> data = new ArrayList<>();
        List<List<Float>> data2 = new ArrayList<>();
        List<String> tName = new ArrayList<>();
        List<String> time = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        Map<String, TempList> dataMap = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            time.add((i + 1) + "季");
            LocalDateTime endTime = startDateTime.plusMonths(3);
            teacherConsumeVoHandler(startDateTime, dataMap, endTime);
            startDateTime = startDateTime.plusMonths(3);
            if (startDateTime.isAfter(now))
                break;
        }
        return getClassCostVo(vo, data, data2, tName, time, dataMap);
    }

    /**
     * 按年份课消统计
     *
     * @param vo 封装的统计条件
     * @return IndexAddAndStreamInfoVo
     */
    private ClassCostVo classCostHandler(StatisticsOfCardCostVo vo) {
        ClassCostVo costVo = new ClassCostVo();
        costVo.setTitle("老师课时消费月统计");
        costVo.setXname("月");
        List<List<Integer>> data = new ArrayList<>();
        List<List<Float>> data2 = new ArrayList<>();
        List<String> tName = new ArrayList<>();
        List<String> time = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(vo.getBeginYear(), 1, 1), startTime);
        Map<String, TempList> dataMap = new HashMap<>();
        for (int i = vo.getBeginYear(); i < vo.getEndYear(); i++) {
            time.add(String.valueOf(i));
            LocalDateTime endTime = startDateTime.plusYears(1);
            teacherConsumeVoHandler(startDateTime, dataMap, endTime);
            startDateTime = startDateTime.plusYears(1);
            if (startDateTime.isAfter(now))
                break;
        }
        return getClassCostVo(costVo, data, data2, tName, time, dataMap);
    }

    private void teacherConsumeVoHandler(LocalDateTime startDateTime, Map<String, TempList> dataMap, LocalDateTime endTime) {
        List<TeacherConsumeVo> teacherConsume = consumeRecordService.getTeacherConsume(startDateTime, endTime);
        teacherConsume.forEach(item -> {
            if (dataMap.containsKey(item.getTeacherName())) {
                TempList tempList = dataMap.get(item.getTeacherName());
                tempList.getCountChanges().add(item.getCountChange());
                tempList.getMoneyCosts().add(item.getMoneyCost() == null ? null : item.getMoneyCost().floatValue());
            } else {
                TempList tempList = new TempList();
                //
                List<Integer> counts = new ArrayList<>();
                counts.add(item.getCountChange());
                //
                List<Float> moneys = new ArrayList<>();
                moneys.add(item.getMoneyCost() == null ? null : item.getMoneyCost().floatValue());
                //
                tempList.setCountChanges(counts);
                tempList.setMoneyCosts(moneys);
                dataMap.put(item.getTeacherName(), tempList);
            }
        });
    }

    /**
     * 填充数据
     *
     * @param vo      封装的查询条件
     * @param data    老师的课消次数集合
     * @param data2   老师的课消金额集合
     * @param tName   老师名字集合
     * @param time    X轴
     * @param dataMap 从数据库查询出来的数据封装进集合
     * @return ClassCostVo 封装的数据返回对象
     */
    private ClassCostVo getClassCostVo(ClassCostVo vo, List<List<Integer>> data, List<List<Float>> data2, List<String> tName, List<String> time, Map<String, TempList> dataMap) {
        Set<String> keySet = dataMap.keySet();
        for (String s : keySet) {
            TempList tempList = dataMap.get(s);
            tName.add(s);
            data.add(tempList.getCountChanges());
            data2.add(tempList.getMoneyCosts());
        }

        vo.setTname(tName);
        vo.setTime(time);
        vo.setData(data);
        vo.setData2(data2);
        return vo;
    }
}
