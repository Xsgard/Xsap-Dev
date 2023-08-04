package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberCardDao;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.service.MemberCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private void setDao(MemberCardDao cardDao) {
        this.cardDao = cardDao;
    }

    @Override
    public List<Long> getMemberCardIdList() {
        return cardDao.getCardIdList();
    }
}
