package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.dto.BindCardInfoDto;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.utils.R;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberCardService
 * @date 2023/8/2 16:31
 */
public interface MemberCardService extends IService<MemberCardEntity> {
    List<Long> getMemberCardIdList();

    void memberBind(BindingResult bindingResult, BindCardInfoDto info);

    R addCard(@Valid MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult);

    void editCard(@Valid MemberCardEntity cardEntity, Long[] courseListStr);

    List<MemberCardEntity> getCardList(Long memberId);

    R getCardTip(Long cardId, Long memberId, Long scheduleId);

    List<MemberCardEntity> getActiveCards(Long memberId);

    void activeOpt(Long memberId, Long bindId, Integer status, String operatorName);
}
