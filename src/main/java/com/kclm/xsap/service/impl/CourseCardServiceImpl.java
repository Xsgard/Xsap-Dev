package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.CourseCardDao;
import com.kclm.xsap.entity.CourseCardEntity;
import com.kclm.xsap.service.CourseCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author Asgard
 * @version 1.0
 * @description: CourseCardServiceImpl
 * @date 2023/8/2 16:37
 */
@Service
public class CourseCardServiceImpl extends ServiceImpl<CourseCardDao, CourseCardEntity> implements CourseCardService {

    private CourseCardDao courseCardDao;

    @Autowired
    private void setDao(CourseCardDao cardDao) {
        this.courseCardDao = cardDao;
    }

    @Override
    public List<Long> getCourseIdList(Long cardId) {
        return courseCardDao.getCourseIdList(cardId);
    }

    /**
     * 添加课程会员卡绑定
     *
     * @param entities 课程会员卡绑定实体
     * @return true or false
     */
    @Override
    public boolean insertCourseCard(List<CourseCardEntity> entities) {
        entities.forEach(item -> courseCardDao.insertCourseCard(item.getCardId(), item.getCourseId()));
        return true;
    }

    /**
     * 删除课程-会员卡绑定
     *
     * @param cardId 会员卡Id
     * @return true or false
     */
    @Override
    public boolean deleteCourseCard(Long cardId) {
        int i = courseCardDao.deleteCourseCard(cardId);
        return i > 0;
    }

    /**
     * 查找课程对应可消费的会员卡
     *
     * @param courseId 课程Id
     * @return 课程Id集合
     */
    @Override
    public List<Long> getCardIdList(Long courseId) {
        return courseCardDao.getCardIdList(courseId);
    }

    /**
     * 删除课程-会员卡绑定
     *
     * @param id 课程Id
     * @return true or false
     */
    @Override
    public boolean deleteCardCourse(Long id) {
        int i = courseCardDao.deleteCardCourse(id);
        return i > 0;
    }
}
