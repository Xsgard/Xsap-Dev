package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.ConsumeRecordDao;
import com.kclm.xsap.entity.ConsumeRecordEntity;
import com.kclm.xsap.service.ConsumeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: ConsumeRecordServiceImpl
 * @date 2023/8/2 16:36
 */
@Service
public class ConsumeRecordServiceImpl extends ServiceImpl<ConsumeRecordDao, ConsumeRecordEntity> implements ConsumeRecordService {
    @Autowired
    private ConsumeRecordService consumeRecordService;

    @Override
    public BigDecimal getMoneyCostPlus(Long memberBindId) {
        LambdaQueryWrapper<ConsumeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeRecordEntity::getMemberBindId, memberBindId);
        List<ConsumeRecordEntity> consumeRecords = consumeRecordService.list(queryWrapper);

        return BigDecimal.valueOf(consumeRecords.stream()
                .filter(item -> !item.getOperateType().equals("绑卡操作"))
                .mapToDouble(e -> e.getMoneyCost().doubleValue())
                .sum());
    }
}
