package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.dao.RechargeRecordDao;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.vo.*;
import com.kclm.xsap.vo.index.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.statistics.ClassCostVo;
import com.kclm.xsap.vo.statistics.StatisticsOfCardCostVo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {
    private static final String TITLE = "新增与流失统计";
    //
    private static final LocalTime startTime = LocalTime.of(0, 0, 0);
    //
    private static final LocalTime endTime = LocalTime.of(23, 59, 59);


    private MemberService memberService;
    private MemberBindRecordService memberBindRecordService;
    private ConsumeRecordService consumeRecordService;
    private RechargeRecordService rechargeRecordService;
    private RechargeRecordDao rechargeRecordDao;

    @Autowired
    public void setDao(RechargeRecordDao rechargeRecordDao) {
        this.rechargeRecordDao = rechargeRecordDao;
    }

    @Autowired
    public void setMemberBindRecordService(MemberBindRecordService memberBindRecordService,
                                           ConsumeRecordService consumeRecordService,
                                           MemberService memberService,
                                           RechargeRecordService rechargeRecordService) {
        this.rechargeRecordService = rechargeRecordService;
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

    /**
     * 收费统计
     *
     * @param vo 封装的查询条件
     * @return IndexAddAndStreamInfoVo
     */
    @Override
    public IndexAddAndStreamInfoVo cardCostMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {

        if (vo.getUnit() == MONTH) {
            //统计时段 --月
            return cardCostHandler(vo.getYearOfSelect());
        } else if (vo.getUnit() == SEASON) {
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
        if (vo.getUnit() == MONTH) {
            //课消统计时段 --月
            return classCostHandler(vo.getYearOfSelect());
        } else if (vo.getUnit() == SEASON) {
            //课消统计时段 --季
            return classCostHandler(vo.getYearOfSelect(), vo.getUnit());
        } else {
            //课消统计时段 --年
            return classCostHandler(vo);
        }
    }

    /**
     * 总课次统计
     *
     * @param vo 封装的查询条件
     * @return IndexAddAndStreamInfoVo
     */
    @Override
    public IndexAddAndStreamInfoVo classCountMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        if (vo.getUnit() == MONTH) {
            //统计时段 --月
            return classCountHandler(vo.getYearOfSelect());
        } else if (vo.getUnit() == SEASON) {
            //统计时段 --季
            return classCountHandler(vo.getYearOfSelect(), vo.getUnit());
        } else {
            //统计时段 --年
            return classCountHandler(vo);
        }
    }

    /**
     * 新增与流失统计
     *
     * @param vo 封装的查询条件
     * @return IndexAddAndStreamInfoVo
     */
    @Override
    public IndexAddAndStreamInfoVo addAndStreamCountMonthOrSeasonOrYear(StatisticsOfCardCostVo vo) {
        if (vo.getUnit() == MONTH) {
            //统计时段 --月
            return addAndStreamCount(vo.getYearOfSelect());
        } else if (vo.getUnit() == SEASON) {
            //统计时段 --季
            return addAndStreamCount(vo.getYearOfSelect(), vo.getUnit());
        } else {
            //统计时段 --年
            return addAndStreamCount(vo);
        }
    }

    /**
     * 收费统计
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
        List<Integer> data;
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
     * 收费统计
     * 按季度收费统计
     *
     * @param year 查找年份
     * @param unit 标志为（季度）重载
     * @return IndexAddAndStreamInfoVo
     */
    private IndexAddAndStreamInfoVo cardCostHandler(Integer year, Integer unit) {
        log.info(String.valueOf(unit));
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
            List<Integer> rechargeList = rechargeRecordService.getRechargeList(startDateTime, endDateTime);
            int sum = rechargeList.stream()
                    .mapToInt(item -> item)
                    .sum();
            data.add(sum);
            startDateTime = startDateTime.plusMonths(3);
            if (endDateTime.isAfter(now))
                break;
        }
        result.setTime(time);
        result.setData(data);
        return result;
    }

    /**
     * 收费统计
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
            int sum = rechargeRecordService.getRechargeListForSeason(start, end)
                    .stream().mapToInt(item -> item).sum();
            data.add(sum);
            start = start.plusYears(1);
        }
        result.setTime(time);
        result.setData(data);

        return result;
    }


    /**
     * 课消统计
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
            LocalDateTime endDateTime = startDateTime.plusMonths(1);
            teacherConsumeVoHandler(startDateTime, dataMap, endDateTime);
            startDateTime = startDateTime.plusMonths(1);
            if (startDateTime.isAfter(now))
                break;
        }
        return getClassCostVo(vo, data, data2, tName, time, dataMap);
    }

    /**
     * 课消统计
     * 按季度课消统计
     *
     * @param year 查找年份
     * @param unit 标志为（季度）重载
     * @return IndexAddAndStreamInfoVo
     */
    private ClassCostVo classCostHandler(Integer year, Integer unit) {
        log.info(unit.toString());
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
            LocalDateTime endDateTime = startDateTime.plusMonths(3);
            teacherConsumeVoHandler(startDateTime, dataMap, endDateTime);
            startDateTime = startDateTime.plusMonths(3);
            if (startDateTime.isAfter(now))
                break;
        }
        return getClassCostVo(vo, data, data2, tName, time, dataMap);
    }

    /**
     * 课消统计
     * 按年份课消统计
     *
     * @param vo 封装的统计条件
     * @return IndexAddAndStreamInfoVo
     */
    private ClassCostVo classCostHandler(StatisticsOfCardCostVo vo) {
        ClassCostVo costVo = new ClassCostVo();
        costVo.setTitle("老师课时消费年统计");
        costVo.setXname("年");
        List<List<Integer>> data = new ArrayList<>();
        List<List<Float>> data2 = new ArrayList<>();
        List<String> tName = new ArrayList<>();
        List<String> time = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(vo.getBeginYear(), 1, 1), startTime);
        Map<String, TempList> dataMap = new HashMap<>();
        for (int i = vo.getBeginYear(); i <= vo.getEndYear(); i++) {
            time.add(String.valueOf(i));
            LocalDateTime endDateTime = startDateTime.plusYears(1);
            teacherConsumeVoHandler(startDateTime, dataMap, endDateTime);
            startDateTime = startDateTime.plusYears(1);
            if (startDateTime.isAfter(now))
                break;
        }
        return getClassCostVo(costVo, data, data2, tName, time, dataMap);
    }

    /**
     * 总课次统计
     * 按月份课消统计
     *
     * @param year 查找年份
     * @return IndexAddAndStreamInfoVo
     */
    public IndexAddAndStreamInfoVo classCountHandler(Integer year) {
        IndexAddAndStreamInfoVo vo = new IndexAddAndStreamInfoVo();
        vo.setTitle("月课时数统计");
        vo.setXname("月");
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        for (int i = 1; i <= 12; i++) {
            time.add(i + "月");
            LocalDateTime endDateTime = startDateTime.plusMonths(1);
            data.add(consumeRecordService.consumeRecordsBetween(startDateTime, endDateTime));
            startDateTime = startDateTime.plusMonths(1);
            if (endDateTime.isAfter(now))
                break;
        }
        vo.setTime(time);
        vo.setData(data);
        return vo;
    }

    /**
     * 总课次统计
     * 按季度课消统计
     *
     * @param year 查找年份
     * @param unit 标志为（季度）重载
     * @return IndexAddAndStreamInfoVo
     */
    public IndexAddAndStreamInfoVo classCountHandler(Integer year, Integer unit) {
        IndexAddAndStreamInfoVo vo = new IndexAddAndStreamInfoVo();
        vo.setTitle("老师课次季度统计");
        vo.setXname("季度");
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        for (int i = 1; i <= 4; i++) {
            time.add("第" + i + "季度");
            LocalDateTime endDateTime = startDateTime.plusMonths(3);
            data.add(consumeRecordService.consumeRecordsBetween(startDateTime, endDateTime));
            startDateTime = startDateTime.plusMonths(3);
            if (endDateTime.isAfter(now))
                break;
        }
        vo.setTime(time);
        vo.setData(data);
        return vo;
    }

    /**
     * 总课次统计
     * 按年份课消统计
     *
     * @param costVo 封装的统计条件
     * @return IndexAddAndStreamInfoVo
     */
    public IndexAddAndStreamInfoVo classCountHandler(StatisticsOfCardCostVo costVo) {
        IndexAddAndStreamInfoVo vo = new IndexAddAndStreamInfoVo();
        vo.setTitle("老师课次年统计");
        vo.setXname("年");
        List<Integer> data = new ArrayList<>();
        List<String> time = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(costVo.getBeginYear(), 1, 1, 0, 0, 0);
        for (int i = costVo.getBeginYear(); i <= costVo.getEndYear(); i++) {
            time.add(String.valueOf(i));
            LocalDateTime end = start.plusYears(1);
            data.add(consumeRecordService.consumeRecordsBetween(start, end));
            start = start.plusYears(1);
        }
        vo.setTime(time);
        vo.setData(data);

        return vo;
    }

    /**
     * 新增与流失统计
     * 按月份统计
     *
     * @param year 查找年份
     * @return IndexAddAndStreamInfoVo
     */
    public IndexAddAndStreamInfoVo addAndStreamCount(Integer year) {
        IndexAddAndStreamInfoVo result = new IndexAddAndStreamInfoVo();
        result.setTitle(TITLE);
        result.setXname("月");
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Integer> data2 = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        for (int i = 1; i <= 12; i++) {
            time.add(i + "月");
            LocalDateTime endDateTime = startDateTime.plusMonths(1);
            MemberCountVo memberCountVo = memberService.queryMemberBetweenByCondition(startDateTime, endDateTime);
            data.add(memberCountVo.getNewNum());
            data2.add(memberCountVo.getStreamNum());
            startDateTime = startDateTime.plusMonths(1);
            if (endDateTime.isAfter(now))
                break;
        }
        result.setTime(time);
        result.setData(data);
        result.setData2(data2);
        return result;
    }

    /**
     * 新增与流失统计
     * 按季度统计
     *
     * @param year 查找年份
     * @param unit 标志为（季度）重载
     * @return IndexAddAndStreamInfoVo
     */
    public IndexAddAndStreamInfoVo addAndStreamCount(Integer year, Integer unit) {
        IndexAddAndStreamInfoVo result = new IndexAddAndStreamInfoVo();
        result.setTitle(TITLE);
        result.setXname("季度");
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        List<Integer> data2 = new ArrayList<>();
        //
        LocalDateTime now = LocalDateTime.now();
        //
        LocalDateTime startDateTime = TimeUtil.timeTransfer(LocalDate.of(year, 1, 1), startTime);
        for (int i = 1; i <= 4; i++) {
            time.add("第" + i + "季度");
            LocalDateTime endDateTime = startDateTime.plusMonths(3);
            MemberCountVo memberCountVo = memberService.queryMemberBetweenByCondition(startDateTime, endDateTime);
            data.add(memberCountVo.getNewNum());
            data2.add(memberCountVo.getStreamNum());
            startDateTime = startDateTime.plusMonths(3);
            if (endDateTime.isAfter(now))
                break;
        }
        result.setTime(time);
        result.setData(data);
        result.setData2(data2);
        return result;
    }

    /**
     * 新增与流失统计
     * 按年份统计
     *
     * @param costVo 封装的统计条件
     * @return IndexAddAndStreamInfoVo
     */
    public IndexAddAndStreamInfoVo addAndStreamCount(StatisticsOfCardCostVo costVo) {
        IndexAddAndStreamInfoVo result = new IndexAddAndStreamInfoVo();
        result.setTitle(TITLE);
        result.setXname("年");
        List<Integer> data = new ArrayList<>();
        List<Integer> data2 = new ArrayList<>();
        List<String> time = new ArrayList<>();
        LocalDateTime start = LocalDateTime.of(costVo.getBeginYear(), 1, 1, 0, 0, 0);
        for (int i = costVo.getBeginYear(); i <= costVo.getEndYear(); i++) {
            time.add(String.valueOf(i));
            LocalDateTime end = start.plusYears(1);
            MemberCountVo memberCountVo = memberService.queryMemberBetweenByCondition(start, end);
            data.add(memberCountVo.getNewNum());
            data2.add(memberCountVo.getStreamNum());
            start = start.plusYears(1);
        }
        result.setTime(time);
        result.setData(data);
        result.setData2(data2);

        return result;
    }

    /**
     * 课消统计
     * 对数据库查询出的数据进行封装
     *
     * @param startDateTime 开始时间
     * @param dataMap       数据集合
     * @param endTime       结束时间
     */
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
     * 课消统计
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
