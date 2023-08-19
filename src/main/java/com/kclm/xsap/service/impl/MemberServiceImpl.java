package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.dto.ReserveRecordDTO;
import com.kclm.xsap.dto.convert.MemberConvert;
import com.kclm.xsap.entity.*;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.utils.ValidationUtil;
import com.kclm.xsap.vo.ClassInfoVo;
import com.kclm.xsap.vo.ConsumeInfoVo;
import com.kclm.xsap.vo.MemberCountVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberServiceImpl
 * @date 2023/8/2 16:43
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    private MemberCardService cardService;

    private MemberBindRecordService bindRecordService;

    private MemberService memberService;

    private ReservationRecordService reservationRecordService;

    private ScheduleRecordService scheduleRecordService;

    private CourseService courseService;

    private ConsumeRecordService consumeRecordService;

    private ClassRecordService classRecordService;

    private EmployeeService employeeService;

    private MemberCardDao cardDao;

    private MemberDao memberDao;

    @Autowired
    private void setApplicationContext(MemberCardService cardService, MemberBindRecordService bindRecordService,
                                       MemberService memberService, ReservationRecordService reservationRecordService,
                                       ScheduleRecordService scheduleRecordService, CourseService courseService,
                                       ConsumeRecordService consumeRecordService, ClassRecordService classRecordService,
                                       EmployeeService employeeService,
                                       MemberCardDao cardDao, MemberDao memberDao) {
        this.consumeRecordService = consumeRecordService;
        this.classRecordService = classRecordService;
        this.employeeService = employeeService;
        this.memberDao = memberDao;
        this.scheduleRecordService = scheduleRecordService;
        this.courseService = courseService;
        this.reservationRecordService = reservationRecordService;
        this.cardDao = cardDao;
        this.cardService = cardService;
        this.bindRecordService = bindRecordService;
        this.memberService = memberService;
    }

    @Override
    public List<MemberCardDTO> cardInfo(Long id) {
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null, MemberBindRecordEntity::getMemberId, id);
        List<MemberBindRecordEntity> bindRecordEntities = bindRecordService.list(queryWrapper);
        return bindRecordEntities.stream().map((item) -> {
            MemberCardEntity card = cardService.getById(item.getCardId());
            LocalDateTime endTime = TimeUtil.timeSubDays(item.getCreateTime(), item.getValidDay());
            return new MemberCardDTO(
                    item.getId(), card.getName(), card.getType(),
                    item.getValidCount(), item.getValidDay(), endTime,
                    item.getActiveStatus(), null, null
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<MemberDTO> memberDtoList() {
        //查询出MemberEntity的List
        List<MemberEntity> memberEntityList = this.list();
        //使用Stream流操作memberEntityList返回List<MemberDTO>类型
        return memberEntityList.stream().map((item) -> {
            //将MemberEntity转换为MemberDTO
            MemberDTO memberDTO = MemberConvert.INSTANCE.entity2Dto(item);
            memberDTO.setName(item.getName() + "(" + item.getPhone() + ")");
            memberDTO.setJoiningDate(item.getCreateTime());
            //查询条件--> 会员绑定表的memberId等于会员表的id
            LambdaQueryWrapper<MemberBindRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberBindRecordEntity::getMemberId, item.getId());
            //查询
            List<MemberBindRecordEntity> recordEntityList = bindRecordService.list(wrapper);
            //存储会员卡名的集合
            List<String> cardName = new ArrayList<>();
            //遍历record集合
            recordEntityList.forEach((record) -> {
                if (Objects.equals(record.getMemberId(), item.getId())) {
                    //查询条件--> 会员卡表的id等于会员绑定记录表的卡号
                    LambdaQueryWrapper<MemberCardEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(MemberCardEntity::getId, record.getCardId());
                    List<MemberCardEntity> cardEntityList = cardService.list(queryWrapper);
                    cardEntityList.forEach((card) -> {
                        cardName.add(card.getName());
                        memberDTO.setCardNameList(cardName);
                    });
                }
            });
            return memberDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public MemberEntity queryByPhone(String phone) {
        //查询条件，根据手机号查询数据库中是否有重复
        LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(phone), MemberEntity::getPhone, phone);
        //查询到的记录数
        return this.getOne(queryWrapper);
    }

    @Override
    public R login(MemberEntity member, BindingResult bindingResult) {
        //BeanValidation校验前端传入的数据
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        //根据手机号查询数据库中是否有重复记录
        MemberEntity queried = this.queryByPhone(member.getPhone());
        //记录数大于0 或 手机号不满足正则校验
        //if (count > 0 || member.getPhone().matches(phoneRegex)) {
        if (queried != null) {
            return R.error(400, "手机号有误，请检查是否操作有误！");
        } else {
            member.setCreateTime(LocalDateTime.now());
            //保存
            this.save(member);
            return new R().put("data", member);
        }
    }

    @Override
    public R memberEdit(MemberEntity member, BindingResult bindingResult) {
        //BeanValidation校验前端传入的数据
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fe : fieldErrors) {
                System.out.println(fe.getField() + " ==> " + fe.getDefaultMessage());
            }
            return R.error().put("error", fieldErrors);
        } else {
            MemberEntity temp = this.getById(member.getId());
            int count = 0;
            if (!temp.getPhone().equals(member.getPhone())) {
                LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(MemberEntity::getPhone, member.getPhone());
                count = this.count(queryWrapper);
            }
            if (count > 0) {
                throw new BusinessException("电话号码重复，检查是否操作有误！");
            }
            //修改
            boolean b = this.updateById(member);
            if (b)
                return R.ok();
            else
                return R.error();
        }
    }

    @Override
    public List<String> getSupportCardNames(List<Long> cardIds) {
        List<String> supportCardNames = new ArrayList<>();
        cardIds.forEach(item -> supportCardNames.add("「" + cardDao.getSupportCardName(item) + "」"));
        return supportCardNames;
    }

    @Override
    public List<ReserveRecordDTO> getReserveRecordDto(Long memberId) {
        LambdaQueryWrapper<ReservationRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationRecordEntity::getMemberId, memberId);
        List<ReservationRecordEntity> recordEntityList = reservationRecordService.list(queryWrapper);
        return recordEntityList.stream().map(item -> {
            //排课计划信息
            ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(item.getScheduleId());
            //课程信息
            CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
            ReserveRecordDTO dto = new ReserveRecordDTO();
            dto.setCourseName(courseEntity.getName());
            //设置预约时间
            if (item.getLastModifyTime() == null)
                dto.setReserveTime(item.getCreateTime());
            else
                dto.setReserveTime(item.getCreateTime());
            dto.setCardName(item.getCardName());
            dto.setReserveNumbers(item.getReserveNums());
            dto.setTimesCost(courseEntity.getTimesCost());
            //设置操作时间
            if (item.getLastModifyTime() == null)
                dto.setOperateTime(item.getCreateTime());
            else
                dto.setOperateTime(item.getCreateTime());
            dto.setOperator(item.getOperator());
            dto.setReserveNote(item.getNote());
            dto.setReserveStatus(item.getStatus());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MemberEntity> filterAllMemberByTime(LocalDateTime start, LocalDateTime end) {
        List<MemberEntity> allMember = memberDao.getAllMember();
        return allMember.stream().map(item -> {
            if (item.getIsDeleted() == 0) {
                if (item.getCreateTime().isAfter(start) && item.getCreateTime().isBefore(end))
                    return item;
            } else {
                if (item.getLastModifyTime() != null) {
                    if (item.getLastModifyTime().isAfter(start) && item.getLastModifyTime().isBefore(end))
                        return item;
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 会员消费记录
     *
     * @param memberId 会员Id
     * @return List<ConsumeInfoVo>
     */
    @Override
    public List<ConsumeInfoVo> getMemberConsumeList(Long memberId) {
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, memberId);
        List<MemberBindRecordEntity> bindRecordEntities = bindRecordService.list(queryWrapper);
        List<ConsumeInfoVo> voList = new ArrayList<>();
        bindRecordEntities.forEach(item -> {
            //
            ConsumeInfoVo vo = new ConsumeInfoVo();
            //会员卡实体信息
            MemberCardEntity card = cardService.getById(item.getCardId());
            //
            LambdaQueryWrapper<ConsumeRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConsumeRecordEntity::getMemberBindId, item.getId());
            //
            List<ConsumeRecordEntity> consumeList = consumeRecordService.list(wrapper);
            //
            consumeList.forEach(e -> {
                vo.setCardName(card.getName());
                vo.setConsumeId(e.getId());
                vo.setOperateTime(e.getCreateTime());
                vo.setCardCountChange(e.getCardCountChange());
                vo.setTimesRemainder(item.getValidCount());
                vo.setMoneyCostBigD(e.getMoneyCost());
                vo.setMoneyCost(e.getMoneyCost().toString());
                vo.setOperateType(e.getOperateType());
                vo.setOperator(e.getOperator());
                vo.setNote(e.getNote());
                vo.setCreateTime(e.getCreateTime());
                if (e.getLastModifyTime() != null)
                    vo.setLastModifyTime(e.getLastModifyTime());
                voList.add(vo);
            });
        });

        return voList;
    }

    @Override
    public List<ClassInfoVo> getClassInfoList(Long memberId) {
        //
        LambdaQueryWrapper<ClassRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassRecordEntity::getMemberId, memberId);
        List<ClassRecordEntity> classRecords = classRecordService.list(queryWrapper);
        List<ClassInfoVo> voList = new ArrayList<>();
        return classRecords.stream().map(item -> {
            //预约记录
            LambdaQueryWrapper<ReservationRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReservationRecordEntity::getMemberId, item.getMemberId())
                    .eq(ReservationRecordEntity::getScheduleId, item.getScheduleId());
            ReservationRecordEntity reservation = reservationRecordService.getOne(wrapper);
            //预约记录信息
            ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(item.getScheduleId());
            //老师信息
            EmployeeEntity teacher = employeeService.getById(scheduleRecord.getTeacherId());
            //
            CourseEntity course = courseService.getById(scheduleRecord.getCourseId());
            //
            ClassInfoVo vo = new ClassInfoVo();
            vo.setClassRecordId(item.getId());
            vo.setCourseName(course.getName());
            vo.setClassTime(TimeUtil.timeTransfer(scheduleRecord.getStartDate(), scheduleRecord.getClassTime()));
            vo.setTeacherName(teacher.getName());
            vo.setCardName(item.getCardName());
            vo.setClassNumbers(reservation.getReserveNums());
            vo.setTimesCost(course.getTimesCost());
            vo.setComment(item.getComment());
            vo.setCheckStatus(item.getCheckStatus());
            vo.setScheduleStartDate(scheduleRecord.getStartDate());
            vo.setScheduleStartTime(scheduleRecord.getClassTime());

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public MemberCountVo queryMemberBetweenByCondition(LocalDateTime start, LocalDateTime end) {
        MemberCountVo vo = new MemberCountVo();
        List<MemberEntity> members = memberDao.getAllMember();
        //未删除
        Integer newNum = Math.toIntExact(members.stream()
                .filter(m -> m.getIsDeleted() == 0) //过滤出未删除的会员信息
                .filter(e -> e.getCreateTime().isAfter(start) && e.getCreateTime().isBefore(end)) //根据时间过滤
                .count());
        //已删除
        Integer streamMun = Math.toIntExact(members.stream()
                .filter(m -> m.getIsDeleted() == 1) //过滤出已删除的会员信息
                .filter(e -> e.getLastModifyTime().isAfter(start) && e.getLastModifyTime().isBefore(end)) //根据时间过滤
                .count());
        vo.setNewNum(newNum);
        vo.setStreamNum(streamMun);
        return vo;
    }

    @Override
    public void deleteMember(Long memberId) {
        LambdaQueryWrapper<ReservationRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        //查询预约记录条件 --1.会员id --2.预约状态为1（有效）
        queryWrapper.eq(ReservationRecordEntity::getMemberId, memberId)
                .eq(ReservationRecordEntity::getStatus, 1);
        //预约记录
        List<ReservationRecordEntity> reservations = reservationRecordService.list(queryWrapper);
        reservations.forEach(item -> {
            //查询消费记录 是否有对应约课的消费记录
            LambdaQueryWrapper<ConsumeRecordEntity> consumeWrapper = new LambdaQueryWrapper<>();
            //查询条件 --1.排课计划Id  --2.绑卡Id
            consumeWrapper.eq(ConsumeRecordEntity::getScheduleId, item.getScheduleId())
                    .eq(ConsumeRecordEntity::getMemberBindId, item.getCardId());
            //预约记录信息
            ConsumeRecordEntity one = consumeRecordService.getOne(consumeWrapper);
            //为空则没有消费
            if (one == null) {
                throw new BusinessException("该会员还有约课尚未完成扣费！");
            }
        });
        LambdaQueryWrapper<MemberBindRecordEntity> wrapper = new LambdaQueryWrapper<>();
        //查询该会员仍激活的会员卡
        wrapper.eq(MemberBindRecordEntity::getMemberId, memberId)
                .eq(MemberBindRecordEntity::getActiveStatus, 1);
        //绑定的激活的会员卡信息
        List<MemberBindRecordEntity> bindRecords = bindRecordService.list(wrapper);
        if (!bindRecords.isEmpty()) {
            //修改集合中绑定会员卡的状态
            bindRecords = bindRecords.stream()
                    .peek(item -> {
                        item.setActiveStatus(0);
                        item.setLastModifyTime(LocalDateTime.now());
                    })
                    .collect(Collectors.toList());
            //批量更新
            boolean b = bindRecordService.updateBatchById(bindRecords);
            if (!b)
                throw new BusinessException("会员卡信息更新失败！");
        }
        //逻辑删除会员
        boolean b1 = this.removeById(memberId);
        if (!b1)
            throw new BusinessException("会员卡删除失败！");
    }

    @Override
    public List<MemberEntity> getMemberList() {
        List<MemberEntity> memberList = memberService.getMemberList();
        return null;
    }
}
