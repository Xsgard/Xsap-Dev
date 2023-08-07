package com.kclm.xsap.web.controller;

import com.kclm.xsap.dto.ScheduleDetailsDto;
import com.kclm.xsap.dto.ScheduleRecordDto;
import com.kclm.xsap.entity.ScheduleRecordEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.ScheduleRecordService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping("/x_course_schedule_detail.do")
    public String toScheduleDetail(Long id, Model model) {
        model.addAttribute("ID", id);
        return "course/x_course_schedule_detail";
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
    public List<ScheduleRecordDto> getScheduleList(Long start, Long end) {
        return scheduleRecordService.scheduleList();
    }

    @PostMapping("/scheduleDetail.do")
    @ResponseBody
    public R scheduleDetail(Long id) {
        ScheduleDetailsDto scheduleDto = scheduleRecordService.getScheduleDto(id);
        return R.ok().put("data", scheduleDto);
    }
}
