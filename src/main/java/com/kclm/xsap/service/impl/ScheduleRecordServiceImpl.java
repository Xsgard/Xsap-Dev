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
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.vo.ConsumeFormVo;
import com.kclm.xsap.vo.ScheduleForConsumeSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private MemberLogService logService;
    private ConsumeRecordService consumeRecordService;

    @Autowired
    private void setApplicationContext(CourseService courseService, EmployeeService employeeService,
                                       CourseCardService courseCardService, MemberService memberService,
                                       ReservationRecordService reservationRecordService, MemberBindRecordService memberBindRecordService,
                                       ScheduleRecordService scheduleRecordService, ConsumeRecordService consumeRecordService,
                                       ClassRecordService classRecordService, MemberLogService logService) {
        this.logService = logService;
        this.consumeRecordService = consumeRecordService;
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
     */
    @Override
    public void scheduleAdd(ScheduleRecordEntity scheduleRecord) {
        //查询条件
        LambdaQueryWrapper<ScheduleRecordEntity> qw = new LambdaQueryWrapper<ScheduleRecordEntity>()
                .eq(ScheduleRecordEntity::getTeacherId, scheduleRecord.getTeacherId())
                .eq(ScheduleRecordEntity::getStartDate, LocalDateTime.now().toLocalDate());
        CourseEntity course = courseService.getById(scheduleRecord.getCourseId());
        //新增排课开始时间
        LocalDateTime newScheduleStart = TimeUtil.timeTransfer(scheduleRecord.getStartDate(), scheduleRecord.getClassTime());
        //新增排课结束时间
        LocalDateTime newScheduleEnd = newScheduleStart.plusMinutes(course.getDuration());
        //老师今天的预约记录
        List<ScheduleRecordEntity> teacherSchedules = this.list(qw);
        teacherSchedules.forEach(ts -> {
            //新增排课的课程信息
            CourseEntity scheduleCourse = courseService.getById(ts.getCourseId());
            //排课开始时间
            LocalDateTime startTime = TimeUtil.timeTransfer(ts.getStartDate(), ts.getClassTime());
            //排课结束时间
            LocalDateTime endTime = startTime.plusMinutes(scheduleCourse.getDuration());
            if (newScheduleStart.equals(startTime))
                throw new BusinessException("老师课程时间冲突！");
            //结束时间 在已有排课开始时间之后
            if (newScheduleEnd.isAfter(startTime) && newScheduleEnd.isBefore(endTime)) {
                throw new BusinessException("该老师在此时间段已有排课！");
            }
            //新增排课开始时间 在已有排课开始时间之后，结束时间之前
            if (newScheduleStart.isAfter(startTime) && newScheduleStart.isBefore(endTime)) {
                throw new BusinessException("该老师在此时间段已有排课！");
            }
        });
        scheduleRecord.setCreateTime(LocalDateTime.now());
        boolean b = this.save(scheduleRecord);
        if (!b)
            throw new BusinessException("课程添加失败,请联系管理员！");

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
        return getReservedInfoDtos(scheduleId, recordEntityList)
                .stream()
                .filter(item -> item.getClassStartTime().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
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
     * 预约扣费
     *
     * @param vo 前端传递的ConsumeFormVo信息
     */
    @Override
    @Transactional
    public void consumeEnsure(ConsumeFormVo vo) {
        LambdaQueryWrapper<ClassRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassRecordEntity::getScheduleId, vo.getScheduleId())
                .eq(ClassRecordEntity::getMemberId, vo.getMemberId());
        ClassRecordEntity classRecord = classRecordService.getOne(queryWrapper);
        //1为确认上课
        classRecord.setCheckStatus(1);
        classRecord.setLastModifyTime(LocalDateTime.now());
        classRecord.setNote("正常预约客户");
        boolean b = classRecordService.updateById(classRecord);
        if (!b)
            throw new BusinessException("更新课程记录出现异常！");

        //会员卡绑定信息
        MemberBindRecordEntity bindRecord = memberBindRecordService.getById(vo.getCardBindId());
        //减去本次消耗的卡次
        bindRecord.setValidCount(bindRecord.getValidCount() - vo.getCardCountChange());
        boolean b1 = memberBindRecordService.updateById(bindRecord);
        if (!b1)
            throw new BusinessException("更新会员绑定记录出现异常！");

        //操作日志
        MemberLogEntity log = new MemberLogEntity();
        log.setType("会员上课扣费操作");
        log.setInvolveMoney(vo.getAmountOfConsumption());
        log.setOperator(vo.getOperator());
        log.setMemberBindId(vo.getCardBindId());
        log.setCreateTime(LocalDateTime.now());
        log.setCardCountChange(vo.getCardCountChange());
        log.setCardDayChange(vo.getCardDayChange());
        log.setCardActiveStatus(1);
        boolean b2 = logService.save(log);
        if (!b2)
            throw new BusinessException("保存操作记录出现异常！");

        //消费记录
        ConsumeRecordEntity consumeRecord = new ConsumeRecordEntity();
        consumeRecord.setOperateType("会员上课扣费操作");
        consumeRecord.setCardCountChange(vo.getCardCountChange());
        consumeRecord.setCardDayChange(vo.getCardDayChange());
        consumeRecord.setMoneyCost(vo.getAmountOfConsumption());
        consumeRecord.setOperator(vo.getOperator());
        consumeRecord.setNote(vo.getNote());
        consumeRecord.setMemberBindId(vo.getCardBindId());
        consumeRecord.setCreateTime(LocalDateTime.now());
        consumeRecord.setLogId(log.getId());
        consumeRecord.setScheduleId(vo.getScheduleId());
        boolean b3 = consumeRecordService.save(consumeRecord);
        if (!b3)
            throw new BusinessException("保存消费记录出现异常！");
    }

    /**
     * 一键扣费功能
     *
     * @param scheduleId 排课计划Id
     * @param operator   操作人
     */
    @Override
    @Transactional
    public void consumeEnsureAll(Long scheduleId, String operator) {
        //预约记录
        ScheduleRecordEntity schedule = scheduleRecordService.getById(scheduleId);
        //课程信息
        CourseEntity course = courseService.getById(schedule.getCourseId());
        //课程记录集合
        LambdaQueryWrapper<ClassRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassRecordEntity::getScheduleId, scheduleId)
                .eq(ClassRecordEntity::getCheckStatus, 0)
                .eq(ClassRecordEntity::getReserveCheck, 1);
        List<ClassRecordEntity> classRecords = classRecordService.list(queryWrapper);
        //通过Stream流生成Vo集合
        List<ConsumeFormVo> voList = classRecords.stream().map(e -> {
            //查询用户对应的预约记录
            LambdaQueryWrapper<ReservationRecordEntity> qw = new LambdaQueryWrapper<>();
            qw.eq(ReservationRecordEntity::getMemberId, e.getMemberId())
                    .eq(ReservationRecordEntity::getScheduleId, e.getScheduleId());
            ReservationRecordEntity reservation = reservationRecordService.getOne(qw);
            BigDecimal money = consumeRecordService.queryAmountPayable(e.getBindCardId());
            //Vo对象
            ConsumeFormVo vo = new ConsumeFormVo();
            vo.setScheduleId(scheduleId);
            vo.setOperator(operator);
            vo.setMemberId(e.getMemberId());
            vo.setCardBindId(e.getBindCardId());
            vo.setAmountOfConsumption(money.multiply(BigDecimal.valueOf(
                    (long) course.getTimesCost() * reservation.getReserveNums())));
            vo.setCardCountChange(course.getTimesCost() * reservation.getReserveNums());
            vo.setCardDayChange(0);
            vo.setClassId(e.getId());
            vo.setNote("");
            return vo;
        }).collect(Collectors.toList());
        //遍历调用单个扣费
        voList.forEach(this::consumeEnsure);
    }

    /**
     * 获取排课信息
     *
     * @return List<ScheduleForConsumeSearchVo>
     */
    @Override
    public List<ScheduleForConsumeSearchVo> getForConsumeSearch() {
        //当前时间
        LocalDateTime now = LocalDateTime.now();
        //两周前的时间
        LocalDateTime start = now.minusDays(14);
        //查询两周内的排课记录
        LambdaQueryWrapper<ScheduleRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(ScheduleRecordEntity::getCreateTime, start, now);
        List<ScheduleRecordEntity> schedules = scheduleRecordService.list(queryWrapper);
        //对预约记录集合处理
        return schedules.stream().map(item -> {
            //查询课程信息
            CourseEntity course = courseService.getById(item.getCourseId());
            //查询老师信息
            EmployeeEntity teacher = employeeService.getById(item.getTeacherId());
            //返回信息实体
            ScheduleForConsumeSearchVo vo = new ScheduleForConsumeSearchVo();
            vo.setScheduleId(item.getId());
            vo.setCourseName(course.getName());
            vo.setTeacherName(teacher.getName());
            vo.setClassDateTime(TimeUtil.timeTransfer(item.getStartDate(), item.getClassTime()));

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 删除排课计划
     *
     * @param scheduleId 排课计划Id
     */
    @Override
    public void deleteScheduleById(Long scheduleId) {
        //查询该排课是否已有预约
        LambdaQueryWrapper<ReservationRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationRecordEntity::getScheduleId, scheduleId)
                .eq(ReservationRecordEntity::getStatus, 1);
        List<ReservationRecordEntity> reservations = reservationRecordService.list(queryWrapper);
        if (!reservations.isEmpty())
            throw new BusinessException("该排课计划已有预约记录，不能删除！");
        //删除无效预约记录
        LambdaQueryWrapper<ReservationRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationRecordEntity::getScheduleId, scheduleId)
                .eq(ReservationRecordEntity::getStatus, 0);
        reservationRecordService.remove(wrapper);

        boolean b = scheduleRecordService.removeById(scheduleId);
        if (!b)
            throw new BusinessException("排课计划删除失败！");
    }

    /**
     * 复制排课信息
     *
     * @param sourceDate 源时间
     * @param targetDate 目标时间
     */
    @Override
    @Transactional
    public String copySchedules(LocalDate sourceDate, LocalDate targetDate) {
        List<ScheduleRecordEntity> sourceSchedules = getScheduleRecordEntities(sourceDate, targetDate);
        //
        sourceSchedules = sourceSchedules.stream()
                .map(item -> item.setStartDate(targetDate))//设置排课时间为目标时间
                .collect(Collectors.toList());
        //查询目标时间所有排课信息
        LambdaQueryWrapper<ScheduleRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScheduleRecordEntity::getStartDate, targetDate);
        List<ScheduleRecordEntity> targetSchedules = this.list(wrapper);
        List<ScheduleRecordEntity> result = new ArrayList<>();
        //查询到目标时间 排课信息为空
        if (targetSchedules.isEmpty()) {
            //保存
            this.saveBatch(sourceSchedules);
            return "原排课计划共有->" + sourceSchedules.size() + "条,已成功复制->" + sourceSchedules.size() + "条排课！";
        }
        sourceSchedules.forEach(s -> {
            //
            CourseEntity sCourse = courseService.getById(s.getCourseId());
            LocalTime sStart = s.getClassTime();
            LocalTime sEnd = sStart.plusMinutes(sCourse.getDuration());
            AtomicBoolean flag = new AtomicBoolean(true);
            List<ScheduleRecordEntity> teacherSchedule = targetSchedules.stream()
                    .filter(item -> Objects.equals(item.getTeacherId(), s.getTeacherId()))
                    .collect(Collectors.toList());
            teacherSchedule.forEach(t -> {
                //
                CourseEntity tCourse = courseService.getById(t.getCourseId());
                LocalTime tStart = t.getClassTime();
                LocalTime tEnd = tStart.plusMinutes(tCourse.getDuration());
                //s开始时间早于t开始时间 s结束时间晚于t开始时间
                if (sStart.isBefore(tStart) && sEnd.isAfter(tStart)) {
                    flag.set(false);
                }
                //
                if (sStart.isAfter(tStart) && sStart.isBefore(tEnd)) {
                    flag.set(false);
                }
                if (sStart.equals(tStart)) {
                    flag.set(false);
                }
            });
            //
            if (flag.get())
                result.add(s);
        });
        if (result.isEmpty())
            throw new BusinessException("复制时发生错误，请联系管理员!");

        scheduleRecordService.saveBatch(result);
        return "原排课计划共有->" + sourceSchedules.size() + "条,已成功复制->" + result.size() + "条排课！";


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
            reservedInfoDto.setClassStartTime(TimeUtil.timeTransfer(
                    scheduleRecordEntity.getStartDate(), scheduleRecordEntity.getClassTime()));
            return reservedInfoDto;
        }).collect(Collectors.toList());
    }

    private List<ScheduleRecordEntity> getScheduleRecordEntities(LocalDate sourceDate, LocalDate targetDate) {
        //判断是否满足日期要求
        if (targetDate.isBefore(LocalDate.now())) {
            throw new BusinessException("不能将排课复制到过去的时间！");
        }
        //查询源时间的所有排课信息
        LambdaQueryWrapper<ScheduleRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleRecordEntity::getStartDate, sourceDate);
        List<ScheduleRecordEntity> sourceSchedules = this.list(queryWrapper);
        if (sourceSchedules.isEmpty())
            throw new BusinessException("原日期内没有排课记录！");
        return sourceSchedules;
    }

}
