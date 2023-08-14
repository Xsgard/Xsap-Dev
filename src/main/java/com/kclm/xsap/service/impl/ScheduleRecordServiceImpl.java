package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.ScheduleRecordDao;
import com.kclm.xsap.dto.ReservedInfoDto;
import com.kclm.xsap.dto.ReverseClassRecordDto;
import com.kclm.xsap.dto.ScheduleDetailsDto;
import com.kclm.xsap.dto.ScheduleRecordDto;
import com.kclm.xsap.entity.*;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Asgard
 * @version 1.0
 * @description: ScheduleRecordServiceImpl
 * @date 2023/8/2 16:45
 */
@Service
public class ScheduleRecordServiceImpl extends ServiceImpl<ScheduleRecordDao, ScheduleRecordEntity> implements ScheduleRecordService {
    private CourseService courseService;
    private EmployeeService employeeService;
    private CourseCardService courseCardService;
    private MemberService memberService;
    private MemberBindRecordService memberBindRecordService;
    private ReservationRecordService reservationRecordService;
    private ScheduleRecordService scheduleRecordService;
    private ClassRecordService classRecordService;

    @Autowired
    private void setApplicationContext(CourseService courseService, EmployeeService employeeService,
                                       CourseCardService courseCardService, MemberService memberService,
                                       ReservationRecordService reservationRecordService, MemberBindRecordService memberBindRecordService,
                                       ScheduleRecordService scheduleRecordService,
                                       ClassRecordService classRecordService) {
        this.classRecordService = classRecordService;
        this.memberBindRecordService = memberBindRecordService;
        this.scheduleRecordService = scheduleRecordService;
        this.courseService = courseService;
        this.employeeService = employeeService;
        this.courseCardService = courseCardService;
        this.memberService = memberService;
        this.reservationRecordService = reservationRecordService;
    }

    /**
     * 添加排课
     *
     * @param scheduleRecord 排课信息
     * @param bindingResult  校验结果集
     * @return R
     */
    @Override
    public R scheduleAdd(@Valid ScheduleRecordEntity scheduleRecord, BindingResult bindingResult) {
        ValidationUtil.getErrors(bindingResult);
        scheduleRecord.setCreateTime(LocalDateTime.now());
        boolean b = this.save(scheduleRecord);
        if (!b)
            throw new BusinessException("课程添加失败");
        return R.ok();
    }

    /**
     * 获取全部排课信息
     *
     * @return List
     */
    @Override
    public List<ScheduleRecordDto> scheduleList() {
        List<ScheduleRecordEntity> scheduleRecordEntities = this.list();
        List<ScheduleRecordDto> dtoList;
        dtoList = scheduleRecordEntities.stream().map(item -> {
            ScheduleRecordDto dto = new ScheduleRecordDto();
            CourseEntity courseEntity = courseService.getById(item.getCourseId());
            EmployeeEntity teacher = employeeService.getById(item.getTeacherId());
            LocalDateTime start = TimeUtil.timeTransfer(item.getStartDate(), item.getClassTime());
            LocalDateTime end = TimeUtil.toEndTime(start, courseEntity.getDuration());
            String url = "x_course_schedule_detail.do?id=" + item.getId();
            String title = courseEntity.getName() + "「" + teacher.getName() + "」";

            dto.setTitle(title);
            dto.setStart(start);
            dto.setEnd(end);
            dto.setHeight(300);
            dto.setColor(courseEntity.getColor());
            dto.setUrl(url);
            return dto;
        }).collect(Collectors.toList());
        return dtoList;
    }

    /**
     * 获取排课计划详细信息
     *
     * @param id 排课计划Id
     * @return ScheduleDetailsDto 排课信息DTO
     */
    @Override
    public ScheduleDetailsDto getScheduleDto(Long id) {
        //查询排课计划信息
        ScheduleRecordEntity scheduleRecord = this.getById(id);
        //查询课程实体信息
        CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
        //获取支持的卡号集合
        List<Long> cardIds = courseCardService.getCardIdList(scheduleRecord.getCourseId());
        List<String> supportCards = memberService.getSupportCardNames(cardIds);
        //查询教师信息
        EmployeeEntity teacher = employeeService.getById(scheduleRecord.getTeacherId());
        //
        LocalDateTime start = TimeUtil.timeTransfer(scheduleRecord.getStartDate(), scheduleRecord.getClassTime());
        LocalDateTime end = TimeUtil.toEndTime(start, courseEntity.getDuration());
        //设置Dto属性
        ScheduleDetailsDto dto = new ScheduleDetailsDto();
        dto.setCourseName(courseEntity.getName());
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setDuration(courseEntity.getDuration());
        dto.setLimitSex(courseEntity.getLimitSex());
        dto.setLimitAge(courseEntity.getLimitAge());
        dto.setSupportCards(supportCards);
        dto.setTeacherName(teacher.getName());
        dto.setOrderNums(scheduleRecord.getOrderNums());
        dto.setClassNumbers(courseEntity.getContains());
        dto.setTimesCost(courseEntity.getTimesCost());

        return dto;
    }

    /**
     * 获取排课的预约记录（不包括取消的）
     *
     * @param scheduleId 排课计划Id
     * @return List
     */
    @Override
    public List<ReservedInfoDto> getReserveInfoDto(Long scheduleId) {
        //查询预约记录
        LambdaQueryWrapper<ReservationRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationRecordEntity::getScheduleId, scheduleId)
                .eq(ReservationRecordEntity::getStatus, 1);
        List<ReservationRecordEntity> recordEntityList = reservationRecordService.list(queryWrapper);
        return getReservedInfoDtos(scheduleId, recordEntityList);
    }

    /**
     * 获取排课的预约记录（包括取消的）
     *
     * @param scheduleId 排课计划Id
     * @return List
     */
    @Override
    public List<ReservedInfoDto> getAllReserveInfoDto(Long scheduleId) {
        //查询预约记录
        LambdaQueryWrapper<ReservationRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationRecordEntity::getScheduleId, scheduleId);
        List<ReservationRecordEntity> recordEntityList = reservationRecordService.list(queryWrapper);
        return getReservedInfoDtos(scheduleId, recordEntityList);
    }

    /**
     * 获取预约课程记录
     *
     * @param scheduleId 排课计划Id
     * @return List
     */
    @Override
    public List<ReverseClassRecordDto> getReverseClassRecordDto(Long scheduleId) {
        //查询课程记录信息
        LambdaQueryWrapper<ClassRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassRecordEntity::getScheduleId, scheduleId)
                .eq(ClassRecordEntity::getReserveCheck, 1);
        List<ClassRecordEntity> classRecordEntities = classRecordService.list(queryWrapper);
        //预约记录信息
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(scheduleId);
        //课程信息
        CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
        //Stream流操作
        return classRecordEntities.stream().map(item -> {
            //查询用户实体信息
            MemberEntity memberEntity = memberService.getById(item.getMemberId());
            //根据 --memberId + scheduleId-- 查询预约记录信息
            LambdaQueryWrapper<ReservationRecordEntity> qw = new LambdaQueryWrapper<>();
            qw.eq(ReservationRecordEntity::getMemberId, memberEntity.getId())
                    .eq(ReservationRecordEntity::getScheduleId, scheduleId);
            ReservationRecordEntity one = reservationRecordService.getOne(qw);
            //课程信息DTO
            ReverseClassRecordDto dto = new ReverseClassRecordDto();
            dto.setClassRecordId(item.getId());
            dto.setMemberName(memberEntity.getName());
            dto.setMemberPhone(memberEntity.getPhone());
            dto.setCardName(item.getCardName());
            dto.setMemberSex(memberEntity.getSex());
            dto.setMemberBirthday(memberEntity.getBirthday());
            dto.setTimesCost(courseEntity.getTimesCost());
            dto.setReserveNums(one.getReserveNums());
            dto.setOperateTime(one.getLastModifyTime() == null ? one.getCreateTime() : one.getLastModifyTime());
            dto.setCheckStatus(item.getCheckStatus());
            dto.setCardId(item.getBindCardId());
            dto.setMemberId(memberEntity.getId());
            dto.setStartDate(scheduleRecord.getStartDate());
            dto.setClassTime(scheduleRecord.getClassTime());
            dto.setCreateTimes(one.getCreateTime());

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * TODO 计算对应会员卡的单词课程消费金额
     *
     * @param bindCardId 会员卡绑定表Id
     * @return Double 课程金额
     */
    @Override
    public Double queryAmountPayable(Long bindCardId) {
        MemberBindRecordEntity record = memberBindRecordService.getById(bindCardId);
        BigDecimal receivedMoney = record.getReceivedMoney();
        return null;
    }

    /**
     * 获取预约记录信息Dto
     *
     * @param scheduleId       预约记录Id
     * @param recordEntityList 预约记录实体信息集合
     * @return List<ReservedInfoDto>
     */
    private List<ReservedInfoDto> getReservedInfoDtos(Long scheduleId, List<ReservationRecordEntity> recordEntityList) {
        //查询排课记录
        ScheduleRecordEntity scheduleRecordEntity = scheduleRecordService.getById(scheduleId);
        //Stream流操作
        return recordEntityList.stream().map(item -> {
            //会员信息
            MemberEntity memberEntity = memberService.getById(item.getMemberId());
            //课程信息
            CourseEntity courseEntity = courseService.getById(scheduleRecordEntity.getCourseId());

            //Dto对象
            ReservedInfoDto reservedInfoDto = new ReservedInfoDto();
            //设置属性
            reservedInfoDto.setReserveId(item.getId());
            reservedInfoDto.setMemberName(memberEntity.getName());
            reservedInfoDto.setPhone(memberEntity.getPhone());
            reservedInfoDto.setCardName(item.getCardName());
            reservedInfoDto.setReserveNumbers(item.getReserveNums());
            reservedInfoDto.setTimesCost(courseEntity.getTimesCost());
            reservedInfoDto.setOperateTime(item.getCreateTime());
            reservedInfoDto.setOperator(item.getOperator());
            reservedInfoDto.setReserveNote(item.getNote());
            reservedInfoDto.setReserveStatus(item.getStatus());
            return reservedInfoDto;
        }).collect(Collectors.toList());
    }
}
