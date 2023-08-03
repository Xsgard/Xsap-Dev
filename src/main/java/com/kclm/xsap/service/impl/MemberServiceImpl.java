package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberService;
import org.springframework.stereotype.Service;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberServiceImpl
 * @date 2023/8/2 16:43
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
}
