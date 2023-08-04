package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.CourseCardEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: CourseCardDao
 * @date 2023/8/2 16:10
 */
@Mapper
public interface CourseCardDao extends BaseMapper<CourseCardEntity> {
    @Select("select course_id from t_course_card where card_id = #{cardId};")
    List<Long> getCourseIdList(Long cardId);

    @Insert("insert into t_course_card (card_id, course_id) values (#{cardId},#{courseId});")
    boolean insertCourseCard(Long cardId, Long courseId);
}
