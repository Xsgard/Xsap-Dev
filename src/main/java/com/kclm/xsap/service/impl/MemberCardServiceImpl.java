package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.dto.BindCardInfoDto;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberCardServiceImpl
 * @date 2023/8/2 16:41
 */
@Service
public class MemberCardServiceImpl extends ServiceImpl<MemberCardDao, MemberCardEntity> implements MemberCardService {

    private MemberCardDao cardDao;

    private MemberBindRecordService bindRecordService;

    @Autowired
    private void setApplicationContext(MemberCardDao cardDao,
                                       MemberBindRecordService bindRecordService) {
        this.cardDao = cardDao;
        this.bindRecordService = bindRecordService;
    }

    /**
     * 获取会员卡id集合
     *
     * @return List<Long>
     */
    @Override
    public List<Long> getMemberCardIdList() {
        return cardDao.getCardIdList();
    }

    /**
     * 绑定会员卡
     *
     * @param bindingResult 用于BeanValidation
     * @param info          传入的绑定数据的Dto信息
     * @return R
     */
    @Override
    public R memberBind(BindingResult bindingResult, @Valid BindCardInfoDto info) {
        ValidationUtil.getErrors(bindingResult);
        MemberBindRecordEntity bindRecordEntity = new MemberBindRecordEntity();
        BeanUtils.copyProperties(info, bindRecordEntity);
        bindRecordEntity.setCreateTime(LocalDateTime.now());
        bindRecordService.save(bindRecordEntity);
        return R.ok();
    }
}
