package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.dao.RechargeRecordDao;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.*;
import com.kclm.xsap.vo.MemberCardStatisticsVo;
import com.kclm.xsap.vo.MemberCardStatisticsWithTotalDataInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private MemberService memberService;
    private MemberBindRecordService memberBindRecordService;
    private MemberCardService memberCardService;
    private ConsumeRecordService consumeRecordService;

    private RechargeRecordDao rechargeRecordDao;

    @Autowired
    public void setDao(RechargeRecordDao rechargeRecordDao) {
        this.rechargeRecordDao = rechargeRecordDao;
    }

    @Autowired
    public void setMemberBindRecordService(MemberBindRecordService memberBindRecordService, ConsumeRecordService consumeRecordService,
                                           MemberService memberService, MemberCardService memberCardService) {

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

    @Override
    public List<Integer> getYearList() {
        List<LocalDateTime> localDateTimes = rechargeRecordDao.getLocalDateTimes();
        return localDateTimes.stream()
                .map(LocalDateTime::getYear)
                .distinct()
                .collect(Collectors.toList());
    }


}
