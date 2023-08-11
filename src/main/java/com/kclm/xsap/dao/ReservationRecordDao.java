package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.ReservationRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Asgard
 * @version 1.0
 * @description: ReservationRecordDao
 * @date 2023/8/2 16:15
 */
@Mapper
public interface ReservationRecordDao extends BaseMapper<ReservationRecordEntity> {
    @Select("select id from t_reservation_record where member_id=#{memberId} and schedule_id=#{scheduleId};")
    Long getReserveId(Long memberId, Long scheduleId);
}
