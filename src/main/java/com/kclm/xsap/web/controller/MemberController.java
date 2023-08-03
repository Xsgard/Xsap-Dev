package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/3 10:23
 */
@Controller
@RequestMapping("/member")
public class MemberController {

    private MemberService memberService;

    @Autowired
    private void setApplicationContext(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/x_member_list.do")
    public String list_do() {
        return "member/x_member_list";
    }

    @PostMapping("/memberList.do")
    @ResponseBody
    public List<MemberEntity> memberEntityList() {
        return memberService.list();
    }
}
