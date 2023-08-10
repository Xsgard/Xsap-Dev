package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.ReservationRecordDao;
import com.kclm.xsap.entity.*;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: ReservationRecordServiceImpl
 * @date 2023/8/2 16:44
 */
@Service
public class ReservationRecordServiceImpl extends ServiceImpl<ReservationRecordDao, ReservationRecordEntity> implements ReservationRecordService {
    private MemberCardService memberCardService;
    private MemberBindRecordService memberBindRecordService;
    private CourseService courseService;
    private ScheduleRecordService scheduleRecordService;
    private GlobalReservationSetService reservationSetService;
    private ReservationRecordService reservationRecordService;

    @Autowired
    private void setService(MemberCardService memberCardService,
                            MemberBindRecordService memberBindRecordService,
                            CourseService courseService,
                            ScheduleRecordService scheduleRecordService,
                            GlobalReservationSetService reservationSetService,
                            ReservationRecordService reservationRecordService) {
        this.reservationRecordService = reservationRecordService;
        this.courseService = courseService;
        this.memberCardService = memberCardService;
        this.memberBindRecordService = memberBindRecordService;
        this.scheduleRecordService = scheduleRecordService;
        this.reservationSetService = reservationSetService;
    }

    //TODO 添加预约时判断会员卡是否支持本课程
    @Override
    public void addReserve(ReservationRecordEntity reservationRecord) {
        //会员卡绑定实体
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, reservationRecord.getMemberId())
                .eq(MemberBindRecordEntity::getCardId, reservationRecord.getCardId());
        MemberBindRecordEntity memberBindRecord = memberBindRecordService.getOne(queryWrapper);
        //校验卡是否激活
        if (memberBindRecord.getActiveStatus() != 1)
            throw new BusinessException("该卡未激活，不能进行消费！");
        //排课计划实体
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(reservationRecord.getScheduleId());
        //课程实体信息
        CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
        LocalDateTime valDay = TimeUtil.timeSub(memberBindRecord.getCreateTime(), memberBindRecord.getValidDay());
        //校验卡是否在有效期
        if (valDay.isBefore(LocalDateTime.now()))
            throw new BusinessException("该卡已不在有效期！");
        //校验剩余课次是否足够消费
        if ((memberBindRecord.getValidCount() - courseEntity.getTimesCost()) < 0) {
            throw new BusinessException("该卡余额不足以消费该课程！");
        }
        GlobalReservationSetEntity globalSet = reservationSetService.getById(1);
        //预约开始时间
        LocalDateTime startTime = TimeUtil.timeTransfer(scheduleRecord.getStartDate(), scheduleRecord.getClassTime());
        LocalDateTime previousDateTime = TimeUtil.timeMinusDay(startTime, globalSet.getStartDay());
        LocalDateTime now = LocalDateTime.now();
        //判断课程是否已经开始
        if (now.isAfter(startTime))
            throw new BusinessException("已经开始上课了，不能预约！");
        //预约开始时间
        if (globalSet.getAppointmentStartMode() == 1) {
            if (now.isAfter(startTime))
                throw new BusinessException("已经开始上课了，不能预约！");
        } else {
            if (now.isBefore(previousDateTime))
                throw new BusinessException("还未到预约开放日期！");
        }
        //预约截止时间
        if (globalSet.getAppointmentDeadlineMode() == 2) {
            LocalDateTime endTime = TimeUtil.timeMinusHour(startTime, globalSet.getEndHour());
            if (now.isAfter(endTime)) {
                throw new BusinessException("已经过了最晚预约日期！");
            }
        } else if (globalSet.getAppointmentDeadlineMode() == 3) {
            LocalDateTime endDay = TimeUtil.timeTransfer(LocalDate.from(
                    TimeUtil.timeMinusDay(startTime, globalSet.getEndDay())), globalSet.getEndTime());
            if (now.isAfter(endDay)) {
                throw new BusinessException("已经过了最晚预约日期！");
            }
        }
        //是否预约过该课程
        LambdaQueryWrapper<ReservationRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationRecordEntity::getMemberId, reservationRecord.getMemberId())
                .eq(ReservationRecordEntity::getScheduleId, reservationRecord.getScheduleId())
                .eq(ReservationRecordEntity::getCardId, reservationRecord.getCardId());
        ReservationRecordEntity one = reservationRecordService.getOne(wrapper);
        //flag 为true 查询到预约记录
        boolean flag = one != null;
        //已经有预约记录
        if (one != null && one.getStatus() == 1) {
            throw new BusinessException("您已预约过该课程！");
        }
        //取消次数超过限制的
        if (one != null && one.getCancelTimes() > courseEntity.getLimitCounts()) {
            throw new BusinessException("您有过多次重复预约并取消本课程的操作，已限制您继续预约该课程！");
        }
        //课堂容量是否足够
        if (reservationRecord.getReserveNums() > (courseEntity.getContains() - scheduleRecord.getOrderNums()))
            throw new BusinessException("该课程太过火爆，已经预约不了了！");
        //设置预约记录属性
        reservationRecord.setCreateTime(LocalDateTime.now());
        reservationRecord.setStatus(1);
        //保存排课记录中已预约人数的变化
        scheduleRecord.setOrderNums(scheduleRecord.getOrderNums() + reservationRecord.getReserveNums());
        boolean saved = scheduleRecordService.updateById(scheduleRecord);
        if (!saved)
            throw new BusinessException("保存失败，请联系管理员！");
        //保存记录
        boolean b;
        if (!flag) {
            b = this.save(reservationRecord);
        } else {
            one.setStatus(1);
            one.setLastModifyTime(LocalDateTime.now());
            b = this.updateById(one);
        }
        if (!b) {
            throw new BusinessException("保存失败，请联系管理员！");
        }

    }

    //TODO 加上取消时间判断是否课程已经开始
    @Override
    public void cancelReserve(Long reserveId) {
        ReservationRecordEntity reservationRecord = reservationRecordService.getById(reserveId);
        reservationRecord.setStatus(0);
        reservationRecord.setCancelTimes(reservationRecord.getCancelTimes() + 1);
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(reservationRecord.getScheduleId());
        scheduleRecord.setOrderNums(scheduleRecord.getOrderNums() - reservationRecord.getReserveNums());
        GlobalReservationSetEntity set = reservationSetService.getById(1);
        //对全局设置中的属性进行判断
        LocalDateTime startTime = TimeUtil.timeTransfer(scheduleRecord.getStartDate(), scheduleRecord.getClassTime());
        LocalDateTime now = LocalDateTime.now();
        if (set.getAppointmentCancelMode() == 1) {
            //对取消无限制，只需在上课时间之前都可以取消
            if (now.isAfter(startTime))
                throw new BusinessException("课程已经开始了，不能取消！");
        } else if (set.getAppointmentCancelMode() == 2) {
            //在 set中读出可以提前取消的小时数进行判断
            LocalDateTime cancelTime = TimeUtil.timeMinusHour(startTime, set.getCancelHour());
            if (now.isAfter(cancelTime))
                throw new BusinessException("已经过了最晚取消预约时间，不能取消！");
        } else {
            //对set中给定的取消的时间点进行判断
            LocalDateTime cancelDateTime = TimeUtil.timeTransfer(
                    TimeUtil.timeMinusDay(startTime, set.getCancelDay()).toLocalDate(), set.getCancelTime());
            if (now.isAfter(cancelDateTime))
                throw new BusinessException("已经过了最晚取消预约时间，不能取消！");
        }
        boolean flag = scheduleRecordService.updateById(scheduleRecord);
        if (!flag) {
            throw new BusinessException("保存失败！");
        }
        boolean b = reservationRecordService.updateById(reservationRecord);
        if (!b) {
            throw new BusinessException("保存失败！");
        }
    }

}
