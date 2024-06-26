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
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.vo.ConsumeFormVo;
import com.kclm.xsap.vo.OperateRecordVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

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
    private CourseCardService courseCardService;
    private CourseService courseService;
    private ScheduleRecordService scheduleRecordService;
    private MemberCardService memberCardService;
    private MemberBindRecordService memberBindRecordService;
    private MemberLogService memberLogService;
    private ClassRecordService classRecordService;
    private ConsumeRecordService consumeRecordService;
    private RechargeRecordService rechargeRecordService;

    @Autowired
    private void setDao(MemberCardDao cardDao, MemberBindRecordDao memberBindRecordDao) {
        this.cardDao = cardDao;
        this.memberBindRecordDao = memberBindRecordDao;
    }

    @Autowired
    private void setService(CourseCardService courseCardService,
                            MemberCardService memberCardService,
                            MemberBindRecordService memberBindRecordService,
                            CourseService courseService,
                            ScheduleRecordService scheduleRecordService,
                            MemberLogService memberLogService,
                            ConsumeRecordService consumeRecordService,
                            RechargeRecordService rechargeRecordService,
                            ClassRecordService classRecordService) {
        this.classRecordService = classRecordService;
        this.rechargeRecordService = rechargeRecordService;
        this.consumeRecordService = consumeRecordService;
        this.memberCardService = memberCardService;
        this.courseService = courseService;
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
    public void memberBind(BindCardInfoDto info, BindingResult bindingResult) {
        MemberBindRecordEntity bindRecordEntity = new MemberBindRecordEntity();
        BeanUtils.copyProperties(info, bindRecordEntity);
        bindRecordEntity.setReceivedMoney(BigDecimal.valueOf(info.getReceivedMoney()));

        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getMemberId, bindRecordEntity.getMemberId())
                .and(wrapper -> wrapper.eq(MemberBindRecordEntity::getCardId, bindRecordEntity.getCardId()));
        List<MemberBindRecordEntity> list = memberBindRecordService.list(queryWrapper);
        if (!list.isEmpty()) {
            throw new BusinessException("此卡已经绑定到该用户，请勿重复绑定！");
        }
        MemberCardEntity cardEntity = memberCardService.getById(info.getCardId());
        //加上会员卡默认的次数
        bindRecordEntity.setValidCount((info.getValidCount()) + cardEntity.getTotalCount());
        //加上会员卡默认天数
        bindRecordEntity.setValidDay((info.getValidDay() + cardEntity.getTotalDay()));
        bindRecordEntity.setCreateTime(LocalDateTime.now());
        memberBindRecordService.save(bindRecordEntity);
        //操作记录日志信息
        String operateType = "绑卡操作";
        MemberLogEntity log = new MemberLogEntity();
        //绑卡日志
        log.setType(operateType);
        log.setInvolveMoney(BigDecimal.valueOf(0));
        log.setOperator(info.getOperator());
        log.setMemberBindId(bindRecordEntity.getId());
        log.setCreateTime(LocalDateTime.now());
        log.setCardCountChange(cardEntity.getTotalCount());
        log.setCardDayChange(cardEntity.getTotalDay());
        log.setCardActiveStatus(1);
        boolean b = memberLogService.save(log);
        String logSaveFail = "日志保存失败";
        if (!b)
            throw new BusinessException(logSaveFail);
        //绑卡充值日志
        if (info.getReceivedMoney() != null) {
            MemberLogEntity logBind = new MemberLogEntity();
            operateType = "绑卡充值操作";
            logBind.setType(operateType);
            logBind.setInvolveMoney(BigDecimal.valueOf(info.getReceivedMoney()));
            logBind.setOperator(info.getOperator());
            logBind.setMemberBindId(bindRecordEntity.getId());
            logBind.setCreateTime(LocalDateTime.now());
            logBind.setCardCountChange(info.getValidCount());
            logBind.setCardDayChange(info.getValidDay());
            logBind.setCardActiveStatus(1);
            boolean b1 = memberLogService.save(logBind);
            if (!b1)
                throw new BusinessException(logSaveFail);
        }


        //消费记录
        ConsumeRecordEntity consumeRecord = new ConsumeRecordEntity();
        consumeRecord.setOperateType("绑卡操作");
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
        //充值记录
        if (info.getReceivedMoney() != null) {
            RechargeRecordEntity rechargeRecord = new RechargeRecordEntity();
            rechargeRecord.setAddCount(info.getValidCount());
            rechargeRecord.setAddDay(info.getValidDay());
            rechargeRecord.setReceivedMoney(BigDecimal.valueOf(info.getReceivedMoney()));
            rechargeRecord.setPayMode(info.getPayMode());
            rechargeRecord.setOperator(info.getOperator());
            rechargeRecord.setNote(info.getNote());
            rechargeRecord.setMemberBindId(bindRecordEntity.getId());
            rechargeRecord.setCreateTime(LocalDateTime.now());
            rechargeRecord.setLogId(log.getId());

            boolean save = rechargeRecordService.save(rechargeRecord);
            if (!save)
                throw new BusinessException("绑卡充值失败！");
        }
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
        MemberBindRecordEntity memberBindRecord = memberBindRecordService.getById(bindId);
        memberBindRecord.setActiveStatus(status);
        boolean b = memberBindRecordService.updateById(memberBindRecord);
        if (!b)
            throw new BusinessException("修改状态失败！");
        //写入日志
        MemberLogEntity log = new MemberLogEntity();
        String note;
        if (status == 1)
            note = "激活会员卡操作";
        else
            note = "停用会员卡操作";
        log.setNote(note);
        log.setType(note);
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
     * 获取该卡的可使用剩余次数 和 课程中每次消费多少课次
     *
     * @param memberId   会员Id
     * @param bindCardId 绑卡Id
     * @return List<OperateRecordVo>
     */
    @Override
    public List<OperateRecordVo> getOperateRecords(Long memberId, Long bindCardId) {
        LambdaQueryWrapper<MemberLogEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberLogEntity::getMemberBindId, bindCardId);
        List<MemberLogEntity> memberLogs = memberLogService.list(queryWrapper);
        return memberLogs.stream().map(item -> {
            OperateRecordVo vo = new OperateRecordVo();
            vo.setId(item.getId());
            vo.setOperateTime(item.getCreateTime());
            vo.setOperateType(item.getType());
            vo.setChangeCount(item.getCardCountChange());
            vo.setChangeMoney(String.valueOf(item.getInvolveMoney()));
            vo.setOperator(item.getOperator());
            vo.setCardNote(item.getNote());
            vo.setStatus(item.getCardActiveStatus());

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 手动扣费
     *
     * @param vo 扣费信息
     */
    @Override
    @Transactional
    public void consumeOpt(ConsumeFormVo vo) {
        if (vo.getCardDayChange() < 0) {
            throw new BusinessException("您输入的扣除课次不能 小于0");
        }
        if (vo.getAmountOfConsumption().doubleValue() < 0)
            throw new BusinessException("您输入的扣除金额不能 小于0");
        MemberBindRecordEntity bindRecord = memberBindRecordService.getById(vo.getCardBindId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validDateTime = TimeUtil.timeSubDays(bindRecord.getCreateTime(), bindRecord.getValidDay());
        //有效期判断
        if (validDateTime.isBefore(now))
            throw new BusinessException("该会员卡已过有效期！");
        //剩余卡次判断
        if (bindRecord.getValidCount() - vo.getCardCountChange() < 0)
            throw new BusinessException("该会员卡剩余课次不足，请先充值！");
        //减去扣除课次
        bindRecord.setValidCount(bindRecord.getValidCount() - vo.getCardCountChange());
        //设置修改时间
        bindRecord.setLastModifyTime(LocalDateTime.now());
        //更新信息
        boolean b = memberBindRecordService.updateById(bindRecord);
        if (!b)
            throw new BusinessException("扣费失败，请重试或联系管理员！");
        //日志信息
        MemberLogEntity log = new MemberLogEntity();
        log.setType("会员上课扣费操作");
        log.setInvolveMoney(vo.getAmountOfConsumption());
        log.setOperator(vo.getOperator());
        log.setMemberBindId(vo.getCardBindId());
        log.setCreateTime(LocalDateTime.now());
        log.setCardCountChange(vo.getCardCountChange());
        log.setCardDayChange(vo.getCardDayChange());
        log.setNote(vo.getNote());
        log.setCardActiveStatus(1);
        //保存日志信息
        boolean save = memberLogService.save(log);
        if (!save)
            throw new BusinessException("保存日志失败！");
        //查询会员卡实体信息
        MemberCardEntity card = this.getById(bindRecord.getCardId());
        //上课记录
        ClassRecordEntity classRecord = new ClassRecordEntity();
        classRecord.setMemberId(vo.getMemberId());
        classRecord.setCardName(card.getName());
        classRecord.setScheduleId(vo.getScheduleId());
        classRecord.setNote("手动添加扣费用户");
        classRecord.setComment(vo.getNote());
        classRecord.setCheckStatus(1);
        classRecord.setReserveCheck(0);
        classRecord.setCreateTime(LocalDateTime.now());
        classRecord.setBindCardId(vo.getCardBindId());
        //保存上课记录
        boolean b1 = classRecordService.save(classRecord);
        if (!b1)
            throw new BusinessException("上课记录保存失败！");
        //消费记录
        ConsumeRecordEntity consumeRecord = new ConsumeRecordEntity();
        consumeRecord.setOperateType("会员上课扣费操作");
        consumeRecord.setCardCountChange(vo.getCardCountChange());
        consumeRecord.setCardDayChange(0);
        consumeRecord.setMoneyCost(vo.getAmountOfConsumption());
        consumeRecord.setOperator(vo.getOperator());
        consumeRecord.setNote(vo.getNote());
        consumeRecord.setMemberBindId(vo.getCardBindId());
        consumeRecord.setCreateTime(LocalDateTime.now());
        consumeRecord.setLogId(log.getId());
        consumeRecord.setScheduleId(vo.getScheduleId());
        //保存消费记录
        boolean saved = consumeRecordService.save(consumeRecord);
        if (!saved)
            throw new BusinessException("消费记录保存失败！");
    }

    /**
     * 添加会员卡业务
     *
     * @param cardEntity    会员卡实体信息
     * @param courseListStr 绑定课程的Id数组
     * @param bindingResult 校验结果集
     */
    @Override
    public void addCard(MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        cardEntity.setCreateTime(LocalDateTime.now());
        boolean b = this.save(cardEntity);
        if (!b)
            throw new BusinessException("添加失败，请联系管理员！");
        List<CourseCardEntity> courseCardEntities = toList(cardEntity, courseListStr);
        boolean b1 = courseCardService.insertCourseCard(courseCardEntities);
        if (!b1)
            throw new BusinessException("添加失败，请联系管理员！");

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
    public void editCard(MemberCardEntity cardEntity, Long[] courseListStr) {
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
     * 删除会员卡
     *
     * @param cardId 会员卡Id
     */
    @Override
    @Transactional
    public void deleteOneCard(Long cardId) {
        //查询绑定该卡的绑定记录
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MemberBindRecordEntity::getCardId, cardId);
        //绑定记录集合
        List<MemberBindRecordEntity> bindRecords = memberBindRecordService.list(queryWrapper);
        //如果绑定记录中仍有激活的就抛出异常
        if (bindRecords.stream().anyMatch(item -> item.getActiveStatus() == 1))
            throw new BusinessException("仍有会员绑定并激活该会员卡，不能删除");
        //获取会员卡实体信息
        MemberCardEntity card = memberCardService.getById(cardId);
        card.setLastModifyTime(LocalDateTime.now());
        memberCardService.updateById(card);
        //保存信息
        boolean save = memberCardService.removeById(card);
        if (!save)
            throw new BusinessException("删除会员卡失败！");
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
