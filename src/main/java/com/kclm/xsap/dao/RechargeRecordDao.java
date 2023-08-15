package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.RechargeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: RechargeRecordDao
 * @date 2023/8/2 16:14
 */
@Mapper
public interface RechargeRecordDao extends BaseMapper<RechargeRecordEntity> {
    @Select("select create_time from t_recharge_record;")
    List<LocalDateTime> getLocalDateTimes();
}
