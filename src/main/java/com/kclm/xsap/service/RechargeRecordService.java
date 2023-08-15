package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.entity.RechargeRecordEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: RechargeRecordService
 * @date 2023/8/2 16:33
 */
public interface RechargeRecordService extends IService<RechargeRecordEntity> {
    List<Integer> getRechargeList(LocalDateTime start, LocalDateTime end);
}
