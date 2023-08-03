package com.kclm.xsap.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/3 10:02
 */
@Controller
@RequestMapping("/index")
public class IndexController {

    @GetMapping
    public String toIndex() {
        return "index";
    }

}
