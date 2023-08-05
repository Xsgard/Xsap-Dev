package com.kclm.xsap.web.controller;

import org.springframework.stereotype.Controller;
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

    @GetMapping("/x_member_card_bind.do")
    public String toCardBind() {
        return "member/x_member_card_bind";
    }
}
