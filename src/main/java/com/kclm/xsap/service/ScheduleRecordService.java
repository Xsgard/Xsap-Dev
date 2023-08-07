package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.entity.ScheduleRecordEntity;
import com.kclm.xsap.utils.R;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;

/**
 * @author Asgard
 * @version 1.0
 * @description: ScheduleRecordService
 * @date 2023/8/2 16:34
 */
public interface ScheduleRecordService extends IService<ScheduleRecordEntity> {
    R scheduleAdd(@Valid ScheduleRecordEntity scheduleRecord, BindingResult bindingResult);
}
