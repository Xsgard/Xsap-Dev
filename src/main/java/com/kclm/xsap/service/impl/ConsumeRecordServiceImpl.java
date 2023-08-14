package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.ConsumeRecordDao;
import com.kclm.xsap.entity.ConsumeRecordEntity;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.service.ConsumeRecordService;
import com.kclm.xsap.service.MemberBindRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Autowired
    private MemberBindRecordService memberBindRecordService;

    @Override
    public BigDecimal queryAmountPayable(Long bindCardId) {
        MemberBindRecordEntity record = memberBindRecordService.getById(bindCardId);
        BigDecimal receivedMoney = record.getReceivedMoney();
        LambdaQueryWrapper<ConsumeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeRecordEntity::getMemberBindId, bindCardId);
        List<ConsumeRecordEntity> consumeRecords = consumeRecordService.list(queryWrapper);

        BigDecimal moneyCostPlus = BigDecimal.valueOf(consumeRecords.stream()
                .filter(item -> !item.getOperateType().equals("绑卡操作"))
                .mapToDouble(e -> e.getMoneyCost().doubleValue())
                .sum());
        double validCount = record.getValidCount().doubleValue();

        return receivedMoney.subtract(moneyCostPlus)
                .divide(BigDecimal.valueOf(validCount), 0, RoundingMode.DOWN);
    }


}
