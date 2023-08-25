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
import com.kclm.xsap.service.ScheduleRecordService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.TimeUtil;
import com.kclm.xsap.utils.ValidationUtil;
import com.kclm.xsap.vo.ConsumeFormVo;
import com.kclm.xsap.vo.ScheduleForConsumeSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private ConsumeRecordService consumeRecordService;

    @Autowired
    private void setApplicationContext(ScheduleRecordService scheduleRecordService, CourseService courseService,
                                       ConsumeRecordService consumeRecordService) {
        this.consumeRecordService = consumeRecordService;
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

    //添加排课记录
    @PostMapping("/scheduleAdd.do")
    @ResponseBody
    public R addSchedule(@Valid ScheduleRecordEntity scheduleRecord, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        try {
            scheduleRecordService.scheduleAdd(scheduleRecord);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("添加成功！");
    }

    //获取全部排课记录
    @PostMapping("/scheduleList.do")
    @ResponseBody
    public List<ScheduleRecordDto> getScheduleList(Long start, Long end) {
        return scheduleRecordService.scheduleList();
    }

    //根据Id获取排课的详细信息
    @PostMapping("/scheduleDetail.do")
    @ResponseBody
    public R scheduleDetail(Long id) {
        ScheduleDetailsDto scheduleDto = scheduleRecordService.getScheduleDto(id);
        return R.ok().put("data", scheduleDto);
    }

    //排课的预约记录信息
    @PostMapping("/reservedList.do")
    @ResponseBody
    public R reserveList(Long id) {
        List<ReservedInfoDto> reserveInfoDto = scheduleRecordService.getReserveInfoDto(id);
        return R.ok().put("data", reserveInfoDto);
    }

    //排课的全部预约记录信息（包括取消的）
    @PostMapping("/reserveRecord.do")
    @ResponseBody
    public R reverseAllList(Long id) {
        List<ReservedInfoDto> allReserveInfoDto = scheduleRecordService.getAllReserveInfoDto(id);
        return R.ok().put("data", allReserveInfoDto);
    }

    //上课记录信息
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

    //单次扣费
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

    //一键扣费
    @PostMapping("/consumeEnsureAll.do")
    @ResponseBody
    public R consumeEnsureAll(Long scheduleId, String operators) {
        try {
            scheduleRecordService.consumeEnsureAll(scheduleId, operators);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("一件扣费成功！");
    }

    //
    @GetMapping("/toSearch.do")
    @ResponseBody
    public R toSearch() {
        List<ScheduleForConsumeSearchVo> forConsumeSearch = scheduleRecordService.getForConsumeSearch();
        return R.ok().put("value", forConsumeSearch);
    }

    //删除排课计划
    @PostMapping("/deleteOne.do")
    @ResponseBody
    public R deleteOne(Long id) {
        try {
            scheduleRecordService.deleteScheduleById(id);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("排课计划删除失败！");
    }

    @PostMapping("/scheduleCopy.do")
    @ResponseBody
    public R scheduleCopy(String sourceDateStr, String targetDateStr) {
        LocalDate sourceDate = TimeUtil.subStringToLocalDate(sourceDateStr);
        LocalDate targetDate = TimeUtil.subStringToLocalDate(targetDateStr);
        try {
            String result = scheduleRecordService.copySchedules(sourceDate, targetDate);
            if (result != null)
                return R.ok(result);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok();
    }
}
