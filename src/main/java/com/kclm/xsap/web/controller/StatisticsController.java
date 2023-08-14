package com.kclm.xsap.web.controller;

import com.kclm.xsap.utils.R;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理 /statistics 请求
 * @date 2023/8/14 17:27
 */
@RequestMapping("/statistics")
@Controller
public class StatisticsController {

    @GetMapping("/x_card_list_stat.html")
    public String toCardListStatPage() {
        return "statistics/x_card_list_stat";
    }

    @PostMapping("/cardInfo.do")
    @ResponseBody
    public R getCardInfo() {

        return null;
    }
}
