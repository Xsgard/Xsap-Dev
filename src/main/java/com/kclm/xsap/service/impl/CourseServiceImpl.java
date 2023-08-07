package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.CourseCardDao;
import com.kclm.xsap.dao.CourseDao;
import com.kclm.xsap.entity.CourseCardEntity;
import com.kclm.xsap.entity.CourseEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.CourseCardService;
import com.kclm.xsap.service.CourseService;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: CourseServiceImpl
 * @date 2023/8/2 16:38
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseDao, CourseEntity> implements CourseService {
    private CourseCardDao courseCardDao;

    private CourseCardService courseCardService;

    @Autowired
    private void setApplicationContext(CourseCardDao courseCardDao, CourseCardService courseCardService) {
        this.courseCardDao = courseCardDao;
        this.courseCardService = courseCardService;
    }

    /**
     * 跳转至课程修改界面
     *
     * @param model    用于绑定前端所需的属性
     * @param courseId 课程Id
     */
    @Override
    public void toCourseEditPage(Model model, Long courseId) {
        CourseEntity courseEntity = this.getById(courseId);
        List<Long> cardIdList = courseCardDao.getCardIdList(courseId);
        model.addAttribute("cardCarry", cardIdList);
        model.addAttribute("courseInfo", courseEntity);
    }

    /**
     * 修改课程信息
     *
     * @param course        课程实体信息
     * @param cardListStr   cardId数组
     * @param bindingResult 校验结果集
     */
    @Transactional
    @Override
    public void updateCourse(@Valid CourseEntity course, Long[] cardListStr, BindingResult bindingResult) {
        //校验前端传入的数据
        ValidationUtil.getErrors(bindingResult);
        //修改实体信息 --失败则抛出异常
        course.setLastModifyTime(LocalDateTime.now());
        boolean b = this.updateById(course);
        if (!b) {
            throw new BusinessException("修改课程信息失败！");
        }
        //查询数据表中是否含有该课程号对应的信息
        LambdaQueryWrapper<CourseCardEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCardEntity::getCourseId, course.getId());
        List<CourseCardEntity> cardEntities = courseCardService.list(queryWrapper);
        //查询到信息，将信息删除
        if (!cardEntities.isEmpty()) {
            boolean flag = courseCardService.deleteCardCourse(course.getId());
            if (!flag) {
                throw new BusinessException("删除关联表信息失败！");
            }
        }
        //添加课程与卡号对应的信息
        if (cardListStr != null && cardListStr.length > 0) {
            List<CourseCardEntity> courseCardEntities = transferToCourseCard(course, cardListStr);
            boolean flag = courseCardService.insertCourseCard(courseCardEntities);
            if (!flag) {
                throw new BusinessException("添加关联表信息失败！");
            }
        }
    }

    /**
     * 添加课程信息功能
     *
     * @param course        课程实体信息
     * @param cardListStr   cardId数组
     * @param bindingResult 校验结果集
     */
    @Override
    public void addCourse(CourseEntity course, Long[] cardListStr, BindingResult bindingResult) {
        //校验前端传入的数据
        ValidationUtil.getErrors(bindingResult);
        //保存课程实体信息
        boolean b = this.save(course);
        if (!b) {
            throw new BusinessException("添加失败！");
        }
        //判断
        if (cardListStr != null && cardListStr.length > 0) {
            List<CourseCardEntity> courseCardEntities = transferToCourseCard(course, cardListStr);
            //添加课程与卡号对应的信息
            boolean flag = courseCardService.insertCourseCard(courseCardEntities);
            if (!flag) {
                throw new BusinessException("添加关联表信息失败！");
            }
        }
    }

    /**
     * 转换课程和卡号为 CourseCard
     *
     * @param course      课程实体
     * @param cardListStr cardId数组
     * @return List
     */
    private static List<CourseCardEntity> transferToCourseCard(CourseEntity course, Long[] cardListStr) {
        List<CourseCardEntity> courseCardEntities = new ArrayList<>();
        for (Long l : cardListStr) {
            CourseCardEntity courseCard = new CourseCardEntity(l, course.getId());
            courseCardEntities.add(courseCard);
        }
        return courseCardEntities;
    }

}
