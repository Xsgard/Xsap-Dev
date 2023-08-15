package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.RechargeRecordDao;
import com.kclm.xsap.entity.RechargeRecordEntity;
import com.kclm.xsap.service.RechargeRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: RechargeRecordServiceImpl
 * @date 2023/8/2 16:44
 */
@Service
public class RechargeRecordServiceImpl extends ServiceImpl<RechargeRecordDao, RechargeRecordEntity> implements RechargeRecordService {

    @Override
    public List<Integer> getRechargeList(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<RechargeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(RechargeRecordEntity::getCreateTime, start, end);
        List<RechargeRecordEntity> recharges = this.list(queryWrapper);
        List<Integer> data = new ArrayList<>();
        for (int i = 1; i <= end.getMonthValue(); i++) {
            int finalI = i;
            Integer monthMoney = (int) recharges.stream()
                    .filter(e -> e.getCreateTime().getMonthValue() == finalI)
                    .mapToDouble(item -> item.getReceivedMoney().doubleValue())
                    .sum();
            data.set(i - 1, monthMoney);
        }
        return data;
    }
}
