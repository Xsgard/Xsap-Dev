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

    @Autowired
    private void setService(MemberCardService memberCardService,
                            MemberBindRecordService memberBindRecordService,
                            CourseService courseService,
                            ScheduleRecordService scheduleRecordService,
                            GlobalReservationSetService reservationSetService) {
        this.courseService = courseService;
        this.memberCardService = memberCardService;
        this.memberBindRecordService = memberBindRecordService;
        this.scheduleRecordService = scheduleRecordService;
        this.reservationSetService = reservationSetService;
    }

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
        //设置预约记录属性
        reservationRecord.setCreateTime(LocalDateTime.now());
        reservationRecord.setStatus(1);
        //
        scheduleRecord.setOrderNums(scheduleRecord.getOrderNums() + 1);
        boolean saved = scheduleRecordService.save(scheduleRecord);
        if (!saved)
            throw new BusinessException("保存失败");
        //保存记录
        boolean b = this.save(reservationRecord);
        if (!b) {
            throw new BusinessException("保存失败");
        }

    }
}
