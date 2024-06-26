package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.CourseEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.CourseService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.Arrays;
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

    @Autowired
    private void setApplicationContext(CourseService courseService) {
        this.courseService = courseService;
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

    @GetMapping("/x_course_list_add.do")
    public String toCourseAddPage() {
        return "course/x_course_list_add";
    }

    //获取课程信息
    @GetMapping("/toSearch.do")
    @ResponseBody
    public R toSearch() {
        List<CourseEntity> courseEntities = courseService.list();
        return R.ok().put("value", courseEntities);
    }

    //获取课程信息
    @PostMapping("/courseList.do")
    @ResponseBody
    public R getCourseList() {
        List<CourseEntity> courseEntities = courseService.list();
        return R.ok().put("data", courseEntities);
    }

    //删除课程信息
    @PostMapping("/deleteOne.do")
    @ResponseBody
    public R deleteOneCourse(Long id) {
        try {
            courseService.deleteOne(id);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("删除成功！");
    }

    //修改课程信息
    @PostMapping("/courseEdit.do")
    @ResponseBody
    public R editCourse(@Valid CourseEntity course, BindingResult bindingResult, Long[] cardListStr, Integer limitAgeRadio, Integer limitCountsRadio) {
        //校验前端传入的数据
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        cardListStr = Arrays.stream(cardListStr)
                .filter(item -> item != -1)
                .toArray(Long[]::new);
        try {
            courseService.updateCourse(course, cardListStr, limitAgeRadio, limitCountsRadio);
            return R.ok();
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    //添加课程
    @PostMapping("/courseAdd.do")
    @ResponseBody
    public R addCourse(@Valid CourseEntity course, BindingResult bindingResult, Long[] cardListStr) {
        //校验前端传入的数据
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        try {
            courseService.addCourse(course, cardListStr);
            return R.ok();
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    //根据Id获取课程信息
    @PostMapping("/getOneCourse.do")
    @ResponseBody
    public R getOneCourse(Long id) {
        CourseEntity courseEntity = courseService.getById(id);
        return R.ok().put("data", courseEntity);
    }
}
