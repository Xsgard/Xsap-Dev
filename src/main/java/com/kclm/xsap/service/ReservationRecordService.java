package com.kclm.xsap.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kclm.xsap.entity.ReservationRecordEntity;

/**
 * @author Asgard
 * @version 1.0
 * @description: ReservationRecordService
 * @date 2023/8/2 16:33
 */
public interface ReservationRecordService extends IService<ReservationRecordEntity> {

    void addReserve(ReservationRecordEntity reservationRecord);

    void cancelReserve(Long reserveId);
}
