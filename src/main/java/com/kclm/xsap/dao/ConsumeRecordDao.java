package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.ConsumeRecordEntity;
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

    @Select("select create_time from t_consume_record;")
    List<LocalDateTime> getLocalDateTimes();
}
