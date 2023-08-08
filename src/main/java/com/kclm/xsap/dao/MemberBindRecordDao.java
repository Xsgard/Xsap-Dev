package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberBindRecordDao
 * @date 2023/8/2 16:12
 */
@Mapper
public interface MemberBindRecordDao extends BaseMapper<MemberBindRecordEntity> {
    @Select("select card_id from t_member_bind_record where member_id=#{memberId};")
    List<Long> getCardIds(Long memberId);
}
