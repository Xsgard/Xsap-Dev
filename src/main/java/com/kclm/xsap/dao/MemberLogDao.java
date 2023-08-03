package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.MemberLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberLogDao
 * @date 2023/8/2 16:14
 */
@Mapper
public interface MemberLogDao extends BaseMapper<MemberLogEntity> {
}
