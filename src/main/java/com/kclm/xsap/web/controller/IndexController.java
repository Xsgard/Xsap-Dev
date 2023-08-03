package com.kclm.xsap.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
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
}
