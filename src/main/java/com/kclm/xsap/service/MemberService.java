package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.dto.ReserveRecordDTO;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.vo.ClassInfoVo;
import com.kclm.xsap.vo.ConsumeInfoVo;
import com.kclm.xsap.vo.MemberCountVo;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberService
 * @date 2023/8/2 16:31
 */
public interface MemberService extends IService<MemberEntity> {
    List<MemberCardDTO> cardInfo(Long id);

    List<MemberDTO> memberDtoList();

    MemberEntity queryByPhone(String phone);

    R memberAdd(@Valid MemberEntity member, BindingResult bindingResult);

    R memberEdit(@Valid MemberEntity member, BindingResult bindingResult);

    List<String> getSupportCardNames(List<Long> cardIds);

    List<ReserveRecordDTO> getReserveRecordDto(Long memberId);

    List<MemberEntity> filterAllMemberByTime(LocalDateTime start, LocalDateTime end);

    List<ConsumeInfoVo> getMemberConsumeList(Long memberId);

    List<ClassInfoVo> getClassInfoList(Long memberId);

    MemberCountVo queryMemberBetweenByCondition(LocalDateTime start, LocalDateTime end);

    void deleteMember(Long memberId);
}
