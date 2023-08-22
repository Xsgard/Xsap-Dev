package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.dto.ReservedInfoDto;
import com.kclm.xsap.dto.ReverseClassRecordDto;
import com.kclm.xsap.dto.ScheduleDetailsDto;
import com.kclm.xsap.dto.ScheduleRecordDto;
import com.kclm.xsap.entity.ScheduleRecordEntity;
import com.kclm.xsap.vo.ConsumeFormVo;
import com.kclm.xsap.vo.ScheduleForConsumeSearchVo;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: ScheduleRecordService
 * @date 2023/8/2 16:34
 */
public interface ScheduleRecordService extends IService<ScheduleRecordEntity> {
    void scheduleAdd(ScheduleRecordEntity scheduleRecord);

    List<ScheduleRecordDto> scheduleList();

    ScheduleDetailsDto getScheduleDto(Long id);

    List<ReservedInfoDto> getReserveInfoDto(Long scheduleId);

    List<ReservedInfoDto> getAllReserveInfoDto(Long scheduleId);

    List<ReverseClassRecordDto> getReverseClassRecordDto(Long scheduleId);

    void consumeEnsure(ConsumeFormVo vo);

    void consumeEnsureAll(Long scheduleId, String operator);

    List<ScheduleForConsumeSearchVo> getForConsumeSearch();

    void deleteScheduleById(Long scheduleId);
}
