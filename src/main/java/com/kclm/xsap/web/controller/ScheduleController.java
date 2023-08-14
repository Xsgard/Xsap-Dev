package com.kclm.xsap.web.controller;

import com.kclm.xsap.dto.ReservedInfoDto;
import com.kclm.xsap.dto.ReverseClassRecordDto;
import com.kclm.xsap.dto.ScheduleDetailsDto;
import com.kclm.xsap.dto.ScheduleRecordDto;
import com.kclm.xsap.entity.CourseEntity;
import com.kclm.xsap.entity.ScheduleRecordEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.ConsumeRecordService;
import com.kclm.xsap.service.CourseService;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.ScheduleRecordService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.vo.ConsumeFormVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
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
    private CourseService courseService;
    private MemberBindRecordService memberBindRecordService;
    private ConsumeRecordService consumeRecordService;

    @Autowired
    private void setApplicationContext(ScheduleRecordService scheduleRecordService, CourseService courseService,
                                       MemberBindRecordService memberBindRecordService,
                                       ConsumeRecordService consumeRecordService) {
        this.consumeRecordService = consumeRecordService;
        this.memberBindRecordService = memberBindRecordService;
        this.scheduleRecordService = scheduleRecordService;
        this.courseService = courseService;
    }

    @GetMapping("/x_course_schedule.do")
    public String toCourseSchedule() {
        return "course/x_course_schedule";
    }

    @GetMapping("/x_course_schedule_detail.do")
    public String toScheduleDetail(Long id, Model model) {
        model.addAttribute("ID", id);
        ScheduleRecordEntity scheduleRecord = scheduleRecordService.getById(id);
        CourseEntity courseEntity = courseService.getById(scheduleRecord.getCourseId());
        model.addAttribute("courseLimit", courseEntity.getLimitCounts());
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

    @PostMapping("/reservedList.do")
    @ResponseBody
    public R reserveList(Long id) {
        List<ReservedInfoDto> reserveInfoDto = scheduleRecordService.getReserveInfoDto(id);
        return R.ok().put("data", reserveInfoDto);
    }

    @PostMapping("/reserveRecord.do")
    @ResponseBody
    public R reverseAllList(Long id) {
        List<ReservedInfoDto> allReserveInfoDto = scheduleRecordService.getAllReserveInfoDto(id);
        return R.ok().put("data", allReserveInfoDto);
    }

    @PostMapping("/classRecord.do")
    @ResponseBody
    public R classRecord(Long id) {
        List<ReverseClassRecordDto> reverseClassRecordDto = scheduleRecordService.getReverseClassRecordDto(id);
        return R.ok().put("data", reverseClassRecordDto);
    }

    //获取单节课程的价钱
    @PostMapping("/queryAmountsPayable.do")
    @ResponseBody
    public R queryAmountPayable(Long bindCardId) {
        BigDecimal money = consumeRecordService.queryAmountPayable(bindCardId);
        return R.ok().put("data", money);
    }

    @PostMapping("/consumeEnsure.do")
    @ResponseBody
    public R consumeEnsure(ConsumeFormVo vo) {
        try {
            scheduleRecordService.consumeEnsure(vo);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("扣费成功！");
    }

    @PostMapping("/consumeEnsureAll.do")
    @ResponseBody
    public R consumeEnsureAll(Long scheduleId, String operators) {
        return null;
    }
}
