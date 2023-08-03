package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.CourseCardDao;
import com.kclm.xsap.entity.CourseCardEntity;
import com.kclm.xsap.service.CourseCardService;
import org.springframework.stereotype.Service;

/**
 * @author Asgard
 * @version 1.0
 * @description: CourseCardServiceImpl
 * @date 2023/8/2 16:37
 */
@Service
public class CourseCardServiceImpl extends ServiceImpl<CourseCardDao, CourseCardEntity> implements CourseCardService {
}
