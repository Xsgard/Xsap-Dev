package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.GlobalReservationSetEntity;
import com.kclm.xsap.service.GlobalReservationSetService;
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
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理 /globalSet请求
 * @date 2023/8/8 9:11
 */
@RequestMapping("/globalSet")
@Controller
public class GlobalSetController {
    private GlobalReservationSetService globalReservationSetService;

    @Autowired
    private void setApplicationContext(GlobalReservationSetService globalReservationSetService) {
        this.globalReservationSetService = globalReservationSetService;
    }

    @GetMapping("/x_course_reservation.do")
    public String toCourseReservation(Model model) {
        List<GlobalReservationSetEntity> list = globalReservationSetService.list();
        GlobalReservationSetEntity globalReservationSetEntity = list.get(0);
        model.addAttribute("GLOBAL_SET", globalReservationSetEntity);
        return "course/x_course_reservation";
    }

    @PostMapping("/globalSetUpdate.do")
    @ResponseBody
    public R updateGlobalSet(@Valid GlobalReservationSetEntity setEntity, BindingResult bindingResult) {
        //BeanValidation
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        boolean b = globalReservationSetService.updateById(setEntity);
        if (b)
            return R.ok("修改成功！");
        else
            return R.error("修改失败！");
    }
}
