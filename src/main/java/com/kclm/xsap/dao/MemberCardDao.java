package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.entity.MemberCardEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberCardDao
 * @date 2023/8/2 16:13
 */
@Mapper
public interface MemberCardDao extends BaseMapper<MemberCardEntity> {
    @Select("select id from t_member_card;")
    List<Long> getCardIdList();

    @Select(("select name from t_member_card where id=#{cardId};"))
    String getSupportCardName(Long cardId);
}
