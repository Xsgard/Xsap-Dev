package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.ReservationRecordEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.ReservationRecordService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/reserve请求（预约相关请求）
 * @date 2023/8/8 14:36
 */
@RequestMapping("/reserve")
@Controller
public class ReserveController {

    private ReservationRecordService reservationRecordService;

    @Autowired
    private void setService(ReservationRecordService reservationRecordService) {
        this.reservationRecordService = reservationRecordService;
    }

    @PostMapping("/addReserve.do")
    @ResponseBody
    public R addReserve(ReservationRecordEntity reservationRecord) {
        try {
            reservationRecordService.addReserve(reservationRecord);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/cancelReserve.do")
    @ResponseBody
    public R cancelReserve(Long reserveId) {
        try {
            reservationRecordService.cancelReserve(reserveId);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok();
    }
}
