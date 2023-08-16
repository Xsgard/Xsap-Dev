package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.entity.ConsumeRecordEntity;
import com.kclm.xsap.vo.TeacherConsumeVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: ConsumeRecordService
 * @date 2023/8/2 16:17
 */
public interface ConsumeRecordService extends IService<ConsumeRecordEntity> {
    BigDecimal queryAmountPayable(Long bindCardId);

    Integer queryUsedClassCost(Long bindCardId);

    List<TeacherConsumeVo> getTeacherConsume(LocalDateTime start, LocalDateTime end);

    Integer consumeRecordsBetween(LocalDateTime start, LocalDateTime end);
}
