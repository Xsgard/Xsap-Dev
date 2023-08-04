package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.entity.MemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberDao
 * @date 2023/8/2 16:14
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
    @Select("select * from t_member m " +
            "left JOIN t_member_bind_record r on m.id = r.member_id " +
            "left join t_member_card c on r.card_id = c.id;")
    List<MemberDTO> memberEntiesList();
}
