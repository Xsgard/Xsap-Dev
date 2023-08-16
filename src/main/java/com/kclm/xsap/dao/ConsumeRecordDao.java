package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.ConsumeRecordEntity;
import com.kclm.xsap.vo.TeacherConsumeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: ConsumeRecordDao
 * @date 2023/8/2 16:10
 */
@Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {
    @Select("select sum(money_cost) from t_consume_record where member_bind_id=#{memberBindId};")
    Double getMoneyCostPlus(Long memberBindId);

    @Select("select e.name teacherName,scr.countChange,scr.moneyCost\n" +
            "from t_employee e\n" +
            "         left join (select sc.teacher_id, sum(cr.money_cost) moneyCost, sum(cr.card_count_change) countChange\n" +
            "                    from t_schedule_record sc\n" +
            "                             join t_consume_record cr on sc.id = cr.schedule_id\n" +
            "                    where cr.create_time between #{start} and #{end}\n" +
            "                    group by sc.teacher_id) as scr\n" +
            "                   on e.id = scr.teacher_id\n" +
            "where is_deleted = 0;")
    List<TeacherConsumeVo> getTeacherConsume(LocalDateTime start, LocalDateTime end);
}
