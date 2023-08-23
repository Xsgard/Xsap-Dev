package com.kclm.xsap.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.ReservationRecordDao;
import com.kclm.xsap.dto.ExportReservationDTO;
import com.kclm.xsap.entity.*;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.ExcelExportUtil;
import com.kclm.xsap.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private ClassRecordService classRecordService;
    private CourseCardService courseCardService;
    private MemberService memberService;
    private ReservationRecordDao reservationRecordDao;

    @Autowired
    private void setDao(ReservationRecordDao reservationRecordDao) {
        this.reservationRecordDao = reservationRecordDao;
    }

    @Autowired
    private void setService(MemberCardService memberCardService,
                            MemberBindRecordService memberBindRecordService,
                            CourseService courseService,
                            ScheduleRecordService scheduleRecordService,
                            GlobalReservationSetService reservationSetService,
                            ReservationRecordService reservationRecordService,
                            ClassRecordService classRecordService,
                            CourseCardService courseCardService,
                            MemberService memberService) {
        this.memberService = memberService;
        this.courseCardService = courseCardService;
        this.classRecordService = classRecordService;
        this.reservationRecordService = reservationRecordService;
        this.courseService = courseService;
        this.memberCardService = memberCardService;
        this.memberBindRecordService = memberBindRecordService;
        this.scheduleRecordService = scheduleRecordService;
        this.reservationSetService = reservationSetService;
    }

    /**
     * 添加预约记录
     *
     * @param reservationRecord 预约信息
     */
    @Override
    @Transactional
    public void addReserve(ReservationRecordEntity reservationRecord) {
        //会员卡绑定实体
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, reservationRecord.getMemberId())
                .eq(MemberBindRecordEntity::getCardId, reservationRecord.getCardId());
        MemberBindRecordEntity memberBindRecord = memberBindRecordService.getOne(queryWrapper);
        //会员卡实体细信息
        MemberCardEntity cardEntity = memberCardService.getById(memberBindRecord.getCardId());
        //排课计划实体
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(reservationRecord.getScheduleId());
        //课程实体信息
        CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
        //会员信息
        MemberEntity member = memberService.getById(reservationRecord.getMemberId());

        //校验卡是否激活
        if (memberBindRecord.getActiveStatus() != 1)
            throw new BusinessException("该卡未激活，不能进行消费！");

        LocalDateTime valDay = TimeUtil.timeSubDays(memberBindRecord.getCreateTime(), memberBindRecord.getValidDay());
        //校验本卡是否支持该课程
        LambdaQueryWrapper<CourseCardEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(CourseCardEntity::getCourseId, courseEntity.getId());
        List<CourseCardEntity> courseCard = courseCardService.list(qw);
        long count = courseCard.stream()
                .filter(item -> Objects.equals(item.getCardId(), reservationRecord.getCardId()))
                .count();
        if (count < 1)
            throw new BusinessException("该卡不支持本课程，请换一张会员卡！");
        //校验卡是否在有效期
        if (valDay.isBefore(LocalDateTime.now()))
            throw new BusinessException("该卡已不在有效期！");
        //校验剩余课次是否足够消费
        if ((memberBindRecord.getValidCount() - courseEntity.getTimesCost()) < 0) {
            throw new BusinessException("该卡余额不足以消费该课程！");
        }
        //判断会员是否满足课程要求
        //课程性别限制
        if (!courseEntity.getLimitSex().equals("无限制") && !courseEntity.getLimitSex().equals(member.getSex()))
            throw new BusinessException("很抱歉，您不满足课程的性别要求！");
        //课程年龄限制
        int age = member.getBirthday().until(LocalDate.now()).getYears();
        if (age < courseEntity.getLimitAge())
            throw new BusinessException("很抱歉，您不满足课程的年龄要求");

        //全局设置实体信息
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
        ReservationRecordEntity one = this.getOne(wrapper);
        //flag 为true 查询到预约记录
        boolean flag = one != null;
        //已经有预约记录
        if (one != null && one.getStatus() == 1) {
            throw new BusinessException("您已预约过该课程！");
        }
        //取消次数超过限制的
        if (one != null && one.getCancelTimes() >= courseEntity.getLimitCounts()) {
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
        //查询课程记录信息
        LambdaQueryWrapper<ClassRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ClassRecordEntity::getMemberId, reservationRecord.getMemberId())
                .eq(ClassRecordEntity::getScheduleId, scheduleRecord.getId());
        ClassRecordEntity recordEntity = classRecordService.getOne(recordWrapper);
        //不为空则有修改记录 空则新增记录
        if (recordEntity != null) {
            recordEntity.setReserveCheck(1);
            recordEntity.setLastModifyTime(LocalDateTime.now());
            boolean save = classRecordService.updateById(recordEntity);
            if (!save)
                throw new BusinessException("保存失败，请联系管理员！");
        } else {
            //课程记录实体信息
            ClassRecordEntity classRecordEntity = new ClassRecordEntity();
            classRecordEntity.setMemberId(reservationRecord.getMemberId());
            classRecordEntity.setNote("正常预约客户");
            classRecordEntity.setCardName(cardEntity.getName());
            classRecordEntity.setScheduleId(scheduleRecord.getId());
            classRecordEntity.setCheckStatus(0);
            classRecordEntity.setReserveCheck(1);
            classRecordEntity.setBindCardId(memberBindRecord.getId());
            classRecordEntity.setCreateTime(LocalDateTime.now());
            //保存课程实体信息
            boolean save = classRecordService.save(classRecordEntity);
            if (!save)
                throw new BusinessException("保存失败，请联系管理员！");
        }

        //保存预约记录
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

    //加上取消时间判断是否课程已经开始
    @Override
    @Transactional
    public void cancelReserve(Long reserveId) {
        ReservationRecordEntity reservationRecord = this.getById(reserveId);
        reservationRecord.setStatus(0);
        reservationRecord.setCancelTimes(reservationRecord.getCancelTimes() + 1);
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(reservationRecord.getScheduleId());
        //减去已取消预约的人数
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
        //查询课程记录信息并修改状态
        LambdaQueryWrapper<ClassRecordEntity> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(ClassRecordEntity::getMemberId, reservationRecord.getMemberId())
                .eq(ClassRecordEntity::getScheduleId, scheduleRecord.getId());
        ClassRecordEntity recordEntity = classRecordService.getOne(recordWrapper);
        recordEntity.setReserveCheck(0);
        boolean updated = classRecordService.updateById(recordEntity);
        if (!updated)
            throw new BusinessException("保存失败！");
        boolean flag = scheduleRecordService.updateById(scheduleRecord);
        if (!flag) {
            throw new BusinessException("保存失败！");
        }
        boolean b = this.updateById(reservationRecord);
        if (!b) {
            throw new BusinessException("保存失败！");
        }
    }

    @Override
    public Long getReserveId(Long memberId, Long scheduleId) {
        return reservationRecordDao.getReserveId(memberId, scheduleId);
    }

    @Override
    public void exportReservationListToLocal(LocalDate startDate, LocalDate endDate) {
        //
        List<ReservationRecordEntity> recordEntityList = reservationRecordService.list();
        //
        List<ExportReservationDTO> collect = recordEntityList.stream().map(item -> {
            ScheduleRecordEntity schedule = scheduleRecordService.getById(item.getScheduleId());
            CourseEntity course = courseService.getById(schedule.getCourseId());
            MemberEntity member = memberService.getById(item.getMemberId());
            //
            ExportReservationDTO dto = new ExportReservationDTO();
            //
            dto.setCourseName(course.getName());
            dto.setReserveTime(LocalDateTimeUtil.format(item.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN));
            dto.setMemberName(member.getName());
            dto.setCardName(item.getCardName());
            dto.setReserveNumbers(item.getReserveNums());
            dto.setTimesCost(course.getTimesCost());
            dto.setOperateTime(item.getLastModifyTime() == null ?
                    LocalDateTimeUtil.format(item.getCreateTime(), DatePattern.NORM_DATETIME_PATTERN) :
                    LocalDateTimeUtil.format(item.getLastModifyTime(), DatePattern.NORM_DATETIME_PATTERN));
            dto.setOperator(item.getOperator());
            dto.setReserveNote(item.getNote());
            dto.setReserveStatus(item.getStatus() == 1 ? "有效" : "无效");
            return dto;
        }).collect(Collectors.toList());
        try {
            ExcelExportUtil.excelWriteToLocal(collect);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}