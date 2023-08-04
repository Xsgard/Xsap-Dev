package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.utils.R;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
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

    R login(@Valid MemberEntity member, BindingResult bindingResult);
}
