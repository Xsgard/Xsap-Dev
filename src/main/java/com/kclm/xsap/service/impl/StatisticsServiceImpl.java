package com.kclm.xsap.service.impl;

import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.service.StatisticsService;
import com.kclm.xsap.vo.MemberCardStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: StatisticsServiceImpl
 * @date 2023/8/14 19:50
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    private MemberService memberService;
    private MemberBindRecordService memberBindRecordService;

    @Autowired
    public void setMemberBindRecordService(MemberBindRecordService memberBindRecordService,
                                           MemberService memberService) {
        this.memberService = memberService;
        this.memberBindRecordService = memberBindRecordService;
    }

    /**
     * TODO 会员卡统计
     *
     * @return List
     */
    @Override
    public List<MemberCardStatisticsVo> getCardStatisticsVo() {
        List<MemberEntity> members = memberService.list();
        List<MemberCardStatisticsVo> voList = new ArrayList<>();
        members.stream().forEach(item -> {
            MemberCardStatisticsVo vo = new MemberCardStatisticsVo();
            vo.setMemberName(item.getName());


        });
        return null;
    }


}
