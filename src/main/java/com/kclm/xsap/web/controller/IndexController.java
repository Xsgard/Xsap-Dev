package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.IndexPageService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.vo.indexStatistics.IndexAddAndStreamInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/index请求
 * @date 2023/8/3 10:02
 */
@Controller
@RequestMapping("/index")
public class IndexController {

    private MemberService memberService;

    private IndexPageService indexPageService;

    @Autowired
    public void setService(MemberService memberService, IndexPageService indexPageService) {
        this.memberService = memberService;
        this.indexPageService = indexPageService;
    }

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
    @GetMapping("/homePageInfo/statisticsOfNewAndLostPeople.do")
    @ResponseBody
    public R statisticsOfMemberCard() {
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();
        // 获取本月第一天
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        // 获取今天的最后时间
        int dayOfMonth = now.getDayOfMonth();
        LocalDateTime nowDayOfMonth = now.withDayOfMonth(dayOfMonth).toLocalDate().atTime(23, 59, 59);
        List<MemberEntity> memberEntityList = memberService.filterAllMemberByTime(firstDayOfMonth, nowDayOfMonth);
        IndexAddAndStreamInfoVo addAndStreamInfo = indexPageService.getAddAndStreamInfo(memberEntityList, firstDayOfMonth, nowDayOfMonth);

        return R.ok().put("data", addAndStreamInfo);
    }
}
