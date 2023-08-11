package com.kclm.xsap.web.controller;

import com.kclm.xsap.utils.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/index请求
 * @date 2023/8/3 10:02
 */
@Controller
@RequestMapping("/index")
public class IndexController {

    /**
     * 跳转至Index页面
     *
     * @return index页面
     */
    @GetMapping
    public String toIndex() {
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }

    @GetMapping("/x_index_home.do")
    public String indexHome() {
        return "x_index_home";
    }

    /**
     * TODO 每月会员新增流失图
     *
     * @return R
     */
    @PostMapping("/homePageInfo/statisticsOfMemberCard.do")
    @ResponseBody
    public R statisticsOfMemberCard() {

        return null;
    }
}
