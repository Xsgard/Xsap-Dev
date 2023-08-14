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
import com.kclm.xsap.vo.ConsumeInfoVo;
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

    private MemberCardDao cardDao;

    private MemberDao memberDao;

    @Autowired
    private void setApplicationContext(MemberCardService cardService, MemberBindRecordService bindRecordService,
                                       MemberService memberService, ReservationRecordService reservationRecordService,
                                       ScheduleRecordService scheduleRecordService, CourseService courseService,
                                       ConsumeRecordService consumeRecordService,
                                       MemberCardDao cardDao, MemberDao memberDao) {
        this.consumeRecordService = consumeRecordService;
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
            LocalDateTime endTime = TimeUtil.timeSub(item.getCreateTime(), item.getValidDay());
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
    public List<MemberEntity> getMemberList() {
        List<MemberEntity> memberList = memberService.getMemberList();
        return null;
    }
}
