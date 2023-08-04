package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.entity.CourseCardEntity;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: CourseCardService
 * @date 2023/8/2 16:18
 */
public interface CourseCardService extends IService<CourseCardEntity> {
    List<Long> getCourseIdList(Long cardId);
}
