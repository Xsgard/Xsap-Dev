package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.ScheduleRecordEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.ScheduleRecordService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/schedule请求
 * @date 2023/8/7 15:03
 */
@RequestMapping("/schedule")
@Controller
public class ScheduleController {
    private ScheduleRecordService scheduleRecordService;

    @Autowired
    private void setApplicationContext(ScheduleRecordService scheduleRecordService) {
        this.scheduleRecordService = scheduleRecordService;
    }

    @GetMapping("/x_course_schedule.do")
    public String toCourseSchedule() {
        return "course/x_course_schedule";
    }

    @PostMapping("/scheduleAdd.do")
    @ResponseBody
    public R addSchedule(ScheduleRecordEntity scheduleRecord, BindingResult bindingResult) {
        try {
            return scheduleRecordService.scheduleAdd(scheduleRecord, bindingResult);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    @PostMapping("/scheduleList.do")
    @ResponseBody
    public List<ScheduleRecordEntity> getScheduleList(Long start, Long end) {
        List<ScheduleRecordEntity> scheduleRecordEntities = scheduleRecordService.list();

        return null;
    }
}
