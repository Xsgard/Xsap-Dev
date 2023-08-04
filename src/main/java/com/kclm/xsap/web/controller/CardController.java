package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/4 10:26
 */
@Controller
@RequestMapping("/card")
public class CardController {
    private MemberService memberService;

    private MemberCardService cardService;

    private MemberBindRecordService bindRecordService;

    @Autowired
    private void setApplicationContext(MemberService memberService,
                                       MemberCardService cardService,
                                       MemberBindRecordService bindRecordService) {
        this.memberService = memberService;
        this.cardService = cardService;
        this.bindRecordService = bindRecordService;
    }

    @PostMapping("/cardList.do")
    @ResponseBody
    public R cardList() {
        List<MemberCardEntity> cardEntityList = cardService.list();
        return new R().put("data", cardEntityList);
    }

    @PostMapping("/activeOpt.do")
    @ResponseBody
    public R activeOpt(Long memberId, Long bindId, Integer status) {
        MemberBindRecordEntity record = bindRecordService.getById(bindId);
        record.setActiveStatus(status);
        boolean b = bindRecordService.updateById(record);
        if (b)
            return new R().put("data", status);
        else
            return R.error();
    }
}
