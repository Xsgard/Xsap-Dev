package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.RechargeRecordDao;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberLogEntity;
import com.kclm.xsap.entity.RechargeRecordEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberLogService;
import com.kclm.xsap.service.RechargeRecordService;
import com.kclm.xsap.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: RechargeRecordServiceImpl
 * @date 2023/8/2 16:44
 */
@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordDao, RechargeRecordEntity> implements RechargeRecordService {
    private MemberLogService memberLogService;

    private RechargeRecordService rechargeRecordService;

    private MemberBindRecordService bindRecordService;

    @Autowired
    public void setService(MemberLogService memberLogService, RechargeRecordService rechargeRecordService,
                           MemberBindRecordService bindRecordService) {
        this.bindRecordService = bindRecordService;
        this.rechargeRecordService = rechargeRecordService;
        this.memberLogService = memberLogService;
    }

    /**
     * 获取时间段内充值记录之和
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 充值的金额之和
     */
    @Override
    public List<Integer> getRechargeList(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<RechargeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(RechargeRecordEntity::getCreateTime, start, end);
        List<RechargeRecordEntity> recharges = this.list(queryWrapper);
        List<Integer> data = new ArrayList<>(end.getMonthValue());
        if (!recharges.isEmpty()) {
            for (int i = start.getMonthValue(); i <= start.getMonthValue() + TimeUtil.calculateMonths(start, end); i++) {
                int finalI = i;
                Integer monthMoney = recharges.stream()
                        .filter(e -> e.getCreateTime().getMonthValue() == finalI)
                        .mapToInt(item -> item.getReceivedMoney().intValue())
                        .sum();
                data.add(monthMoney);
            }
        }
        return data;
    }

    /**
     * 获取季度充值之和
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 季度充值之和
     */
    @Override
    public List<Integer> getRechargeListForSeason(LocalDateTime start, LocalDateTime end) {
        //
        LambdaQueryWrapper<RechargeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(RechargeRecordEntity::getCreateTime, start, end);
        //充值记录集合
        List<RechargeRecordEntity> recharges = this.list(queryWrapper);
        //初始化结果集合
        List<Integer> data = new ArrayList<>(end.getMonthValue());
        if (!recharges.isEmpty()) {
            for (int i = start.getMonthValue(); i <= start.getMonthValue() + TimeUtil.calculateMonths(start, end) - 1; i++) {
                int finalI = i;
                Integer monthMoney = recharges.stream()
                        .filter(e -> e.getCreateTime().getMonthValue() == finalI) //过滤出本月的数据
                        .mapToInt(item -> item.getReceivedMoney().intValue())
                        .sum();
                data.add(monthMoney);
            }
        }
        return data;
    }

    /**
     * 添加充值记录
     *
     * @param rechargeRecord 充值信息实体
     */
    @Override
    @Transactional
    public void memberRecharge(RechargeRecordEntity rechargeRecord) {
        //日志信息
        MemberLogEntity log = new MemberLogEntity();
        log.setType("充值操作");
        log.setInvolveMoney(rechargeRecord.getReceivedMoney());
        log.setOperator(rechargeRecord.getOperator());
        log.setMemberBindId(rechargeRecord.getMemberBindId());
        log.setCreateTime(LocalDateTime.now());
        log.setCardCountChange(rechargeRecord.getAddCount());
        log.setCardDayChange(rechargeRecord.getAddDay());
        log.setNote(rechargeRecord.getNote());
        log.setCardActiveStatus(1);
        //保存日志信息 fail则抛出异常
        boolean save = memberLogService.save(log);
        if (!save)
            throw new BusinessException("日志信息保存失败！");
        //设置创建时间
        rechargeRecord.setCreateTime(LocalDateTime.now());
        //设置对应的日志Id
        rechargeRecord.setLogId(log.getId());
        //保存充值信息
        boolean b = rechargeRecordService.save(rechargeRecord);
        if (!b)
            throw new BusinessException("保存充值记录失败！");
        MemberBindRecordEntity bindRecord = bindRecordService.getById(rechargeRecord.getMemberBindId());
        //加上充值次数
        bindRecord.setValidCount(bindRecord.getValidCount() + rechargeRecord.getAddCount());
        //加上充值有效期
        bindRecord.setValidDay(bindRecord.getValidDay() + rechargeRecord.getAddDay());
        //实收金额加上充值金额
        bindRecord.setReceivedMoney(bindRecord.getReceivedMoney().add(rechargeRecord.getReceivedMoney()));
        bindRecord.setLastModifyTime(LocalDateTime.now());
        boolean update = bindRecordService.updateById(bindRecord);
        if (!update)
            throw new BusinessException("充值失败！");
    }


}
