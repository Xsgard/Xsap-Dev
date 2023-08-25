package com.kclm.xsap.web.controller;

import com.kclm.xsap.dto.BindCardInfoDto;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/cardBind请求
 * @date 2023/8/4 19:42
 */
@RequestMapping("/cardBind")
@Controller
public class CardBindController {

    private MemberCardService memberCardService;

    @Autowired
    private void setApplicationContext(MemberCardService memberCardService) {
        this.memberCardService = memberCardService;
    }

    @GetMapping("/x_member_card_bind.do")
    public String toCardBind() {
        return "member/x_member_card_bind";
    }

    @PostMapping("/memberBind.do")
    @ResponseBody
    public R memberBind(@Valid BindCardInfoDto infoDto, BindingResult bindingResult) {
        //BeanValidation
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        //绑定会员卡信息
        try {
            memberCardService.memberBind(infoDto, bindingResult);
            return R.ok();
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }
}
