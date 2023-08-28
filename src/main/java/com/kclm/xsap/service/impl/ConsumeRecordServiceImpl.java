package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.ConsumeRecordDao;
import com.kclm.xsap.entity.ConsumeRecordEntity;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.service.ConsumeRecordService;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.vo.TeacherConsumeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: ConsumeRecordServiceImpl
 * @date 2023/8/2 16:36
 */
@Service
public class ConsumeRecordServiceImpl extends ServiceImpl<ConsumeRecordDao, ConsumeRecordEntity> implements ConsumeRecordService {

    private ConsumeRecordDao consumeRecordDao;

    private MemberBindRecordService memberBindRecordService;

    @Autowired
    public void setConsumeRecordDao(ConsumeRecordDao consumeRecordDao) {
        this.consumeRecordDao = consumeRecordDao;
    }

    @Autowired
    public void setService(MemberBindRecordService memberBindRecordService) {
        this.memberBindRecordService = memberBindRecordService;
    }

    /**
     * 获取课程单价
     *
     * @param bindCardId 绑卡Id
     * @return 课程单价
     */
    @Override
    public BigDecimal queryAmountPayable(Long bindCardId) {
        MemberBindRecordEntity memberBindRecord = memberBindRecordService.getById(bindCardId);
        BigDecimal receivedMoney = memberBindRecord.getReceivedMoney();
        LambdaQueryWrapper<ConsumeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeRecordEntity::getMemberBindId, bindCardId);
        List<ConsumeRecordEntity> consumeRecords = this.list(queryWrapper);

        BigDecimal moneyCostPlus = BigDecimal.valueOf(consumeRecords.stream()
                .filter(item -> !item.getOperateType().equals("绑卡操作"))
                .mapToDouble(e -> e.getMoneyCost().doubleValue())
                .sum());
        double validCount = memberBindRecord.getValidCount().doubleValue();

        return receivedMoney.subtract(moneyCostPlus)
                .divide(BigDecimal.valueOf(validCount), 0, RoundingMode.DOWN);
    }

    /**
     * 查询已用的课消之和
     *
     * @param bindCardId 绑卡Id
     * @return 已用的课消之和
     */
    @Override
    public Integer queryUsedClassCost(Long bindCardId) {
        //绑定记录
        MemberBindRecordEntity memberBindRecord = memberBindRecordService.getById(bindCardId);
        //
        LambdaQueryWrapper<ConsumeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeRecordEntity::getMemberBindId, memberBindRecord.getId());
        //消费记录集合
        List<ConsumeRecordEntity> consumes = this.list(queryWrapper);
        //过滤绑卡操作 对卡次变化求和
        return consumes.stream()
                .filter(e -> !e.getOperateType().equals("绑卡操作"))
                .mapToInt(ConsumeRecordEntity::getCardCountChange)
                .sum();
    }

    /**
     * 获取老师消费记录集合
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 获取对应老师的消费记录集合
     */
    @Override
    public List<TeacherConsumeVo> getTeacherConsume(LocalDateTime start, LocalDateTime end) {
        return consumeRecordDao.getTeacherConsume(start, end);
    }

    /**
     * 获取时间段内消费卡次之和
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 时间段内消费卡次之和
     */
    @Override
    public Integer consumeRecordsBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<ConsumeRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(ConsumeRecordEntity::getCreateTime, start, end);
        List<ConsumeRecordEntity> consumeRecords = this.list(queryWrapper);
        return consumeRecords.stream()
                .filter(e -> !e.getOperateType().equals("绑卡操作"))
                .mapToInt(ConsumeRecordEntity::getCardCountChange)
                .sum();
    }

}
