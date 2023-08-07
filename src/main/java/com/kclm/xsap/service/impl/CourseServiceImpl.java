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
    private void setApplicationContext(CourseCardDao courseCardDao,
                                       CourseCardService courseCardService) {
        this.courseCardDao = courseCardDao;
        this.courseCardService = courseCardService;
    }

    @Override
    public void toCourseEditPage(Model model, Long courseId) {
        CourseEntity courseEntity = this.getById(courseId);
        List<Long> cardIdList = courseCardDao.getCardIdList(courseId);
        model.addAttribute("cardCarry", cardIdList);
        model.addAttribute("courseInfo", courseEntity);
    }

    @Transactional
    @Override
    public void updateCourse(@Valid CourseEntity course, Long[] cardListStr, BindingResult bindingResult) {
        ValidationUtil.getErrors(bindingResult);
        course.setLastModifyTime(LocalDateTime.now());
        boolean b = this.updateById(course);
        if (!b) {
            throw new BusinessException("修改课程信息失败！");
        }
        LambdaQueryWrapper<CourseCardEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCardEntity::getCourseId, course.getId());
        List<CourseCardEntity> cardEntities = courseCardService.list(queryWrapper);
        if (!cardEntities.isEmpty()) {
            boolean flag = courseCardService.deleteCardCourse(course.getId());
            if (!flag) {
                throw new BusinessException("删除关联表信息失败！");
            }
        }

        if (cardListStr != null && cardListStr.length > 0) {
            List<CourseCardEntity> courseCardEntities = transferToCourseCard(course, cardListStr);
            boolean flag = courseCardService.insertCourseCard(courseCardEntities);
            if (!flag) {
                throw new BusinessException("添加关联表信息失败！");
            }
        }

    }

    private static List<CourseCardEntity> transferToCourseCard(CourseEntity course, Long[] cardListStr) {
        List<CourseCardEntity> courseCardEntities = new ArrayList<>();
        for (Long l : cardListStr) {
            CourseCardEntity courseCard = new CourseCardEntity(l, course.getId());
            courseCardEntities.add(courseCard);
        }
        return courseCardEntities;
    }

}
