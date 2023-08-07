package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.dto.BindCardInfoDto;
import com.kclm.xsap.entity.CourseCardEntity;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.CourseCardService;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberCardServiceImpl
 * @date 2023/8/2 16:41
 */
@Service
public class MemberCardServiceImpl extends ServiceImpl<MemberCardDao, MemberCardEntity> implements MemberCardService {

    private MemberCardDao cardDao;

    private MemberBindRecordService bindRecordService;

    private CourseCardService courseCardService;

    @Autowired
    private void setApplicationContext(MemberCardDao cardDao,
                                       MemberBindRecordService bindRecordService,
                                       CourseCardService courseCardService) {
        this.cardDao = cardDao;
        this.bindRecordService = bindRecordService;
        this.courseCardService = courseCardService;
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
    public void memberBind(BindingResult bindingResult, @Valid BindCardInfoDto info) {
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
        bindRecordEntity.setCreateTime(LocalDateTime.now());
        bindRecordService.save(bindRecordEntity);
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
    public R addCard(MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
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
     * 1.校验前端传入的会员卡实体信息并修改
     * 2.删除‘课程-会员卡’绑定表中本卡的绑定信息
     * 3.添加本卡绑定课程信息
     *
     * @param cardEntity    会员卡实体信息
     * @param courseListStr 绑定课程Id数组
     * @param bindingResult 校验结果集
     */
    @Override
    @Transactional
    public void editCard(@Valid MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        //Bean Validation
        ValidationUtil.getErrors(bindingResult);
        //修改实体信息
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
