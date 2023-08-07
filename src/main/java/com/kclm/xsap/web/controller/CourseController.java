package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.CourseEntity;
import com.kclm.xsap.service.CourseCardService;
import com.kclm.xsap.service.CourseService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    private CourseCardService courseCardService;

    private MemberCardService cardService;

    @Autowired
    private void setApplicationContext(CourseService courseService,
                                       MemberCardService cardService,
                                       CourseCardService courseCardService) {
        this.courseService = courseService;
        this.cardService = cardService;
        this.courseCardService = courseCardService;
    }

    @GetMapping("/x_course_list.do")
    public String toCoursePage() {
        return "course/x_course_list";
    }

    @GetMapping("/x_course_list_edit.do")
    public String toCourseEditPage(Long id, Model model) {
        courseService.toCourseEditPage(model, id);
        return "course/x_course_list_edit";
    }


    @PostMapping("/courseList.do")
    @ResponseBody
    public R getCourseList() {
        List<CourseEntity> courseEntities = courseService.list();
        return R.ok().put("data", courseEntities);
    }

    @PostMapping("/deleteOne.do")
    @ResponseBody
    public R deleteOneCourse(Long CourseId) {

        return null;
    }

    @PostMapping("/courseEdit.do")
    public R EditCourse(CourseEntity course, Long[] cardListStr) {

        return null;
    }

}
