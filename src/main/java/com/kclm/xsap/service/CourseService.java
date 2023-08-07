package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.entity.CourseEntity;
import org.springframework.ui.Model;

/**
 * @author Asgard
 * @version 1.0
 * @description: CourseService
 * @date 2023/8/2 16:19
 */
public interface CourseService extends IService<CourseEntity> {
    void toCourseEditPage(Model model, Long courseId);

    void updateCourse(CourseEntity course, Long[] cardListStr);

    void addCourse(CourseEntity course, Long[] cardListStr);
}
