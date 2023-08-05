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

    @Override
    public boolean insertCourseCard(List<CourseCardEntity> entities) {
        entities.forEach((item) -> courseCardDao.insertCourseCard(item.getCardId(), item.getCourseId()));
        return true;
    }

    @Override
    public boolean deleteCourseCard(Long id) {
        int i = courseCardDao.deleteCourseCard(id);
        return i > 0;
    }
}
