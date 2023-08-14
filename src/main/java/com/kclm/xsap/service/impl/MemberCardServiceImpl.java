package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberBindRecordDao;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.dto.BindCardInfoDto;
import com.kclm.xsap.dto.CardTipsDto;
import com.kclm.xsap.entity.*;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.*;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberCardServiceImpl
 * @date 2023/8/2 16:41
 */
@Service
public class MemberCardServiceImpl extends ServiceImpl<MemberCardDao, MemberCardEntity> implements MemberCardService {

    private MemberCardDao cardDao;
    private MemberBindRecordDao memberBindRecordDao;
    private MemberBindRecordService bindRecordService;
    private CourseCardService courseCardService;
    private CourseService courseService;
    private ScheduleRecordService scheduleRecordService;
    private MemberCardService memberCardService;
    private MemberBindRecordService memberBindRecordService;
    private MemberLogService memberLogService;
    private ConsumeRecordService consumeRecordService;

    @Autowired
    private void setDao(MemberCardDao cardDao, MemberBindRecordDao memberBindRecordDao) {
        this.cardDao = cardDao;
        this.memberBindRecordDao = memberBindRecordDao;
    }

    @Autowired
    private void setService(MemberBindRecordService bindRecordService,
                            CourseCardService courseCardService,
                            MemberCardService memberCardService,
                            MemberBindRecordService memberBindRecordService,
                            CourseService courseService,
                            ScheduleRecordService scheduleRecordService,
                            MemberLogService memberLogService,
                            ConsumeRecordService consumeRecordService) {
        this.consumeRecordService = consumeRecordService;
        this.memberCardService = memberCardService;
        this.courseService = courseService;
        this.bindRecordService = bindRecordService;
        this.courseCardService = courseCardService;
        this.memberLogService = memberLogService;
        this.memberBindRecordService = memberBindRecordService;
        this.scheduleRecordService = scheduleRecordService;
    }

    /**
     * 获取会员卡id集合
     *
     * @return List<Long>
     */
    @Override
    public List<Long> getMemberCardIdList() {
        return cardDao.getCardIdList();
    }

    /**
     * 绑定会员卡
     *
     * @param bindingResult 用于BeanValidation
     * @param info          传入的绑定数据的Dto信息
     */
    @Override
    @Transactional
    public void memberBind(@Valid BindCardInfoDto info, BindingResult bindingResult) {
        ValidationUtil.getErrors(bindingResult);
        MemberBindRecordEntity bindRecordEntity = new MemberBindRecordEntity();
        BeanUtils.copyProperties(info, bindRecordEntity);

        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, bindRecordEntity.getMemberId())
                .and(wrapper -> wrapper.eq(MemberBindRecordEntity::getCardId, bindRecordEntity.getCardId()));
        List<MemberBindRecordEntity> list = bindRecordService.list(queryWrapper);
        if (!list.isEmpty()) {
            throw new BusinessException("此卡已经绑定到该用户，请勿重复绑定！");
        }
        MemberCardEntity cardEntity = memberCardService.getById(info.getCardId());
        //加上会员卡默认的次数
        //bindRecordEntity.setValidCount((info.getValidCount()) + cardEntity.getTotalCount());
        //加上会员卡默认天数
        //bindRecordEntity.setValidDay((info.getValidDay() + cardEntity.getTotalDay()));
        bindRecordEntity.setCreateTime(LocalDateTime.now());
        bindRecordService.save(bindRecordEntity);

        //操作记录日志信息
        MemberLogEntity log = new MemberLogEntity();
        log.setType("绑卡操作");
        log.setInvolveMoney(BigDecimal.valueOf(info.getReceivedMoney()));
        log.setOperator(info.getOperator());
        log.setMemberBindId(bindRecordEntity.getId());
        log.setCreateTime(LocalDateTime.now());
        log.setCardCountChange(info.getValidCount());
        log.setCardDayChange(info.getValidDay());
        log.setCardActiveStatus(1);
        boolean b = memberLogService.save(log);
        if (!b)
            throw new BusinessException("日志保存失败！");
        //消费记录
        String operateType = "绑卡操作";
        if (info.getReceivedMoney() != null) {
            operateType = "绑卡充值操作";
        }
        ConsumeRecordEntity consumeRecord = new ConsumeRecordEntity();
        consumeRecord.setOperateType(operateType);
        consumeRecord.setCardCountChange(info.getValidCount());
        consumeRecord.setCardDayChange(info.getValidDay());
        consumeRecord.setMoneyCost(BigDecimal.valueOf(info.getReceivedMoney()));
        consumeRecord.setOperator(info.getOperator());
        consumeRecord.setNote("办卡的费用");
        consumeRecord.setMemberBindId(bindRecordEntity.getId());
        consumeRecord.setCreateTime(LocalDateTime.now());
        boolean saved = consumeRecordService.save(consumeRecord);
        if (!saved)
            throw new BusinessException("消费日志日志保存失败！");
    }

    /**
     * 激活、禁用 卡
     *
     * @param memberId     会员id
     * @param bindId       绑定表id
     * @param status       修改的状态
     * @param operatorName 操作人
     */
    @Override
    @Transactional
    public void activeOpt(Long memberId, Long bindId, Integer status, String operatorName) {
        MemberBindRecordEntity record = bindRecordService.getById(bindId);
        record.setActiveStatus(status);
        boolean b = bindRecordService.updateById(record);
        if (!b)
            throw new BusinessException("修改状态失败！");
        MemberLogEntity log = new MemberLogEntity();
        String note;
        if (status == 1)
            note = "激活会员卡操作";
        else
            note = "停用会员卡操作";
        log.setNote(note);
        log.setInvolveMoney(BigDecimal.valueOf(0));
        log.setOperator(operatorName);
        log.setMemberBindId(bindId);
        log.setCreateTime(LocalDateTime.now());
        log.setCardCountChange(0);
        log.setCardDayChange(0);
        log.setCardActiveStatus(status);
        boolean flag = memberLogService.save(log);
        if (!flag)
            throw new BusinessException("日志保存失败！");
    }

    /**
     * 添加会员卡业务
     *
     * @param cardEntity    会员卡实体信息
     * @param courseListStr 绑定课程的Id数组
     * @param bindingResult 校验结果集
     * @return R
     */
    @Override
    public R addCard(@Valid MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        } else {
            boolean flag = false;
            cardEntity.setCreateTime(LocalDateTime.now());
            boolean b = this.save(cardEntity);
            List<CourseCardEntity> courseCardEntities = toList(cardEntity, courseListStr);
            boolean b1 = courseCardService.insertCourseCard(courseCardEntities);
            if (b && b1) {
                flag = true;
            }
            if (flag)
                return R.ok();
            else
                return R.error();
        }
    }

    /**
     * 修改会员卡业务：
     * 1.删除‘课程-会员卡’绑定表中本卡的绑定信息
     * 2.添加本卡绑定课程信息
     *
     * @param cardEntity    会员卡实体信息
     * @param courseListStr 绑定课程Id数组
     */
    @Override
    @Transactional
    public void editCard(@Valid MemberCardEntity cardEntity, Long[] courseListStr) {
        //修改实体信息
        cardEntity.setLastModifyTime(LocalDateTime.now());
        boolean b = this.updateById(cardEntity);
        if (!b) {
            throw new BusinessException("修改会员卡信息失败！");
        }
        //查询 课程-会员卡 表中是否有本卡Id的信息
        LambdaQueryWrapper<CourseCardEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCardEntity::getCardId, cardEntity.getId());
        List<CourseCardEntity> list = courseCardService.list(queryWrapper);
        if (!list.isEmpty()) {
            //删除信息，如失败则抛出异常
            boolean flag = courseCardService.deleteCourseCard(cardEntity.getId());
            if (!flag)
                throw new BusinessException("删除‘课程-会员卡’表记录出现异常");

        }
        //非空且长度>0
        if (courseListStr != null && courseListStr.length > 0) {
            List<CourseCardEntity> courseCardEntities = toList(cardEntity, courseListStr);
            boolean flag = courseCardService.insertCourseCard(courseCardEntities);
            if (!flag)
                throw new BusinessException("添加‘课程-会员卡’表记录出现异常");
        }
        R.ok();
    }

    /**
     * 获取该会员可用的卡的List
     *
     * @param memberId 会员ID
     * @return List
     */
    @Override
    public List<MemberCardEntity> getCardList(Long memberId) {
        List<Long> cardIds = memberBindRecordDao.getCardIds(memberId);
        return cardIds.stream().map(this::getById)
                .collect(Collectors.toList());
    }

    /**
     * 获取该卡的可使用剩余次数 和 课程中每次消费多少课次
     *
     * @param cardId     会员卡id
     * @param memberId   会员id
     * @param scheduleId 绑定表的id
     * @return R
     */
    @Override
    @Transactional
    public R getCardTip(Long cardId, Long memberId, Long scheduleId) {
        //查询绑定的记录实体
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, memberId)
                .and(qw -> qw.eq(MemberBindRecordEntity::getCardId, cardId));
        MemberBindRecordEntity memberBindRecord = memberBindRecordService.getOne(queryWrapper);
        //未激活的会员卡不能进行消费
        if (memberBindRecord.getActiveStatus() != 1) {
            throw new BusinessException("该卡未激活，不能进行消费！");
        }
        //查询绑定记录实体
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(scheduleId);
        CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
        CardTipsDto cardTips = new CardTipsDto(memberBindRecord.getValidCount(), courseEntity.getTimesCost());
        return R.ok().put("data", cardTips);
    }

    /**
     * 获取该用户绑定的激活的所有卡的实体
     *
     * @param memberId 会员id
     * @return List
     */
    @Override
    public List<MemberCardEntity> getActiveCards(Long memberId) {
        //查询所有该用户绑定的激活的卡的实体
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, memberId)
                .eq(MemberBindRecordEntity::getActiveStatus, 1);
        List<MemberBindRecordEntity> bindRecordEntities = memberBindRecordService.list(queryWrapper);
        //通过Stream流对绑定的卡进行查询
        return bindRecordEntities.stream()
                .map(item -> memberCardService.getById(item.getCardId()))
                .collect(Collectors.toList());
    }


    /**
     * 重构方法：用于创建CourseCardList
     *
     * @param cardEntity    会员卡实体信息
     * @param courseListStr 绑定课程的id信息
     * @return List
     */
    public static List<CourseCardEntity> toList(MemberCardEntity cardEntity, Long[] courseListStr) {
        List<CourseCardEntity> courseCardEntities = new ArrayList<>();
        for (Long courseId : courseListStr) {
            CourseCardEntity courseCardEntity = new CourseCardEntity();
            courseCardEntity.setCardId(cardEntity.getId());
            courseCardEntity.setCourseId(courseId);
            courseCardEntities.add(courseCardEntity);
        }
        return courseCardEntities;
    }
}
