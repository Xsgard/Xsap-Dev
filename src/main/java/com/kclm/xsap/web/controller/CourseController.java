package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.CourseEntity;
import com.kclm.xsap.service.CourseService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/course请求
 * @date 2023/8/4 13:17
 */
@RequestMapping("/course")
@Controller
public class CourseController {

    private CourseService courseService;

    private MemberCardService cardService;

    @Autowired
    private void setApplicationContext(CourseService courseService,
                                       MemberCardService cardService) {
        this.courseService = courseService;
        this.cardService = cardService;
    }


    @PostMapping("/courseList.do")
    @ResponseBody
    public R getCourseList(Model model) {
        List<CourseEntity> courseEntities = courseService.list();

        return new R().put("data", courseEntities);
    }
}
