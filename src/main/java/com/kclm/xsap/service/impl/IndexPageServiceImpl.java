package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.dao.ReservationRecordDao;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.entity.RechargeRecordEntity;
import com.kclm.xsap.service.IndexPageService;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.RechargeRecordService;
import com.kclm.xsap.vo.IndexHomeDateVo;
import com.kclm.xsap.vo.indexStatistics.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.indexStatistics.IndexPieChartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Asgard
 * @version 1.0
 * @description: IndexPageServiceImpl
 * @date 2023/8/12 10:14
 */
@Service
public class IndexPageServiceImpl implements IndexPageService {
    private MemberDao memberDao;
    private ReservationRecordDao reservationRecordDao;

    private MemberCardService memberCardService;

    private MemberBindRecordService memberBindRecordService;

    private RechargeRecordService rechargeRecordService;

    @Autowired
    private void setDao(MemberDao memberDao, ReservationRecordDao reservationRecordDao) {
        this.reservationRecordDao = reservationRecordDao;
        this.memberDao = memberDao;
    }

    @Autowired
    private void setService(MemberCardService memberCardService, RechargeRecordService rechargeRecordService,
                            MemberBindRecordService memberBindRecordService) {
        this.memberBindRecordService = memberBindRecordService;
        this.rechargeRecordService = rechargeRecordService;
        this.memberCardService = memberCardService;
    }

    /**
     * 会员新增流失图
     *
     * @param memberList 会员信息集合
     * @param start      开始时间
     * @param end        本日时间
     * @return IndexAddAndStreamInfoVo
     */
    @Override
    public IndexAddAndStreamInfoVo getAddAndStreamInfo(List<MemberEntity> memberList, LocalDateTime start, LocalDateTime end) {
        IndexAddAndStreamInfoVo vo = new IndexAddAndStreamInfoVo();
        vo.setXname("日");
        int dayOfMonth = end.getDayOfMonth();
        List<String> time = new ArrayList<>();
        Integer[] arr = new Integer[dayOfMonth];
        for (int i = 1; i <= dayOfMonth; i++) {
            time.add(String.valueOf(i));
            arr[i - 1] = 0;
        }
        List<Integer> dataArr = new ArrayList<>(Arrays.asList(arr));
        vo.setTime(time);
        List<Integer> data = memberList.stream()
                .filter(item -> item.getIsDeleted() == 0)
                .map(item -> {
                    int day = item.getCreateTime().getDayOfMonth();
                    dataArr.set(day - 1, dataArr.get(day) + 1);
                    return dataArr;
                }).collect(Collectors.toList()).get(0);

        List<Integer> dataArr2 = new ArrayList<>(Arrays.asList(arr));

        List<Integer> data2 = memberList.stream()
                .filter(item -> item.getIsDeleted() == 1)
                .map(item -> {
                    int day = item.getCreateTime().getDayOfMonth();
                    dataArr2.set(day - 1, dataArr2.get(day) + 1);
                    return dataArr2;
                }).collect(Collectors.toList()).get(0);
        vo.setData(data);
        vo.setData2(data2);
        return vo;
    }

    /**
     * 会员卡统计图
     *
     * @return List<IndexPieChartVo>
     */
    @Override
    public List<IndexPieChartVo> getMemberCards() {
        List<MemberCardEntity> cardEntityList = memberCardService.list();
        return cardEntityList.stream().map(item -> {
            //查询会员卡绑定记录
            LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MemberBindRecordEntity::getCardId, item.getId());
            List<MemberBindRecordEntity> records = memberBindRecordService.list(queryWrapper);
            //初始化vo数据
            IndexPieChartVo vo = new IndexPieChartVo();
            vo.setName(item.getName());
            vo.setValue(records.size());
            return vo;
        }).collect(Collectors.toList());

    }

    /**
     * 当月每日收费统计
     *
     * @return IndexAddAndStreamInfoVo
     */
    @Override
    public IndexAddAndStreamInfoVo DailyCharge() {
        IndexAddAndStreamInfoVo vo = new IndexAddAndStreamInfoVo();
        vo.setTitle("当月每日收费统计");
        vo.setXname("日");
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();
        // 获取本月第一天
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        // 获取今天的最后时间
        int dayOfMonth = now.getDayOfMonth();
        LocalDateTime nowDayOfMonth = now.withDayOfMonth(dayOfMonth)
                .toLocalDate().atTime(23, 59, 59);
        //时间轴
        List<String> time = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < dayOfMonth; i++) {
            time.add(String.valueOf(i + 1));
            data.add(0);
        }
        vo.setTime(time);

        LambdaQueryWrapper<RechargeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(RechargeRecordEntity::getCreateTime, firstDayOfMonth, nowDayOfMonth);
        List<RechargeRecordEntity> recharges = rechargeRecordService.list(queryWrapper);
        for (int i = 0; i < dayOfMonth; i++) {
            int finalI = i;
            Integer money = (int) recharges.stream()
                    .filter(e -> e.getCreateTime().getDayOfMonth() == (finalI + 1))
                    .mapToDouble(item -> item.getReceivedMoney().doubleValue())
                    .sum();
            data.set(i, money);
        }
        vo.setData(data);

        return vo;
    }

    /**
     * 会员计数、活跃会员计数、预约计数
     *
     * @return IndexHoneDateVo
     */
    @Override
    public IndexHomeDateVo getHomeDateVo() {
        IndexHomeDateVo vo = new IndexHomeDateVo();
        //会员计数
        Integer memberCount = memberDao.getMemberCount();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusMonths(1);
        //活跃会员计数
        Integer reserveMemberCount = reservationRecordDao.getReserveMemberCount(start, end);
        //预约计数
        Integer reservationCount = reservationRecordDao.getReservationCount();
        vo.setTotalMembers(memberCount);
        vo.setActiveMembers(reserveMemberCount);
        vo.setTotalReservations(reservationCount);
        return vo;
    }
}
