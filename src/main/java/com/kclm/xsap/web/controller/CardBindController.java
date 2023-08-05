package com.kclm.xsap.web.controller;

import com.kclm.xsap.dto.BindCardInfoDto;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/cardBind请求
 * @date 2023/8/4 19:42
 */
@RequestMapping("/cardBind")
@Controller
public class CardBindController {

    private MemberBindRecordService bindRecordService;

    private MemberCardService memberCardService;

    @Autowired
    private void setApplicationContext(MemberBindRecordService bindRecordService,
                                       MemberCardService memberCardService) {
        this.bindRecordService = bindRecordService;
        this.memberCardService = memberCardService;
    }

    @GetMapping("/x_member_card_bind.do")
    public String toCardBind() {
        return "member/x_member_card_bind";
    }

    public R memberBind(BindingResult bindingResult, BindCardInfoDto infoDto) {
        //绑定会员卡信息
        return memberCardService.memberBind(bindingResult, infoDto);
    }
}
