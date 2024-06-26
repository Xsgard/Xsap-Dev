package com.kclm.xsap.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.entity.MemberEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
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

    @Select("select id,name,sex,phone from t_member;")
    List<MemberEntity> selectMemberList();

    @Select("select create_time,last_modify_time,is_deleted from t_member where is_deleted=0  or is_deleted=1;")
    List<MemberEntity> getAllMember();

    @Select("select name, create_time, last_modify_time, is_deleted\n" +
            "from t_member\n" +
            "where (is_deleted = 0 or is_deleted = 1)\n" +
            "  and (create_time between #{start} and #{end});")
    List<MemberEntity> selectMemberBetween(LocalDateTime start, LocalDateTime end);

    @Select("select count(id) from t_member where is_deleted=0;")
    Integer getMemberCount();
}
