package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberLogDao;
import com.kclm.xsap.entity.MemberLogEntity;
import com.kclm.xsap.service.MemberLogService;
import org.springframework.stereotype.Service;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberLogServiceImpl
 * @date 2023/8/2 16:42
 */
@Service
public class MemberLogServiceImpl extends ServiceImpl<MemberLogDao, MemberLogEntity> implements MemberLogService {
}
