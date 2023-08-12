package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.EmployeeEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.CourseCardService;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: 处理/card请求
 * @date 2023/8/4 10:26
 */
@Controller
@RequestMapping("/card")
public class CardController {
    private MemberService memberService;

    private MemberCardService memberCardService;

    private CourseCardService courseCardService;

    private MemberBindRecordService bindRecordService;

    @Autowired
    private void setApplicationContext(MemberService memberService,
                                       MemberCardService cardService,
                                       MemberBindRecordService bindRecordService,
                                       CourseCardService courseCardService) {
        //
        this.memberService = memberService;
        this.memberCardService = cardService;
        this.bindRecordService = bindRecordService;
        this.courseCardService = courseCardService;
    }

    @GetMapping("/x_member_card.do")
    public String toMemberCard() {
        return "member/x_member_card";
    }

    @GetMapping("/x_member_add_card.do")
    public String toMemberAddCard() {
        return "member/x_member_add_card";
    }

    @PostMapping("/cardList.do")
    @ResponseBody
    public R cardList() {
        List<MemberCardEntity> cardEntityList = memberCardService.list();
        return new R().setData(cardEntityList);
    }

    @GetMapping("/x_member_card_edit.do")
    public String getCardInfoById(Long id, Model model) {
        MemberCardEntity cardEntity = memberCardService.getById(id);
        List<Long> courseIdList = courseCardService.getCourseIdList(id);

        model.addAttribute("courseCarry", courseIdList);
        model.addAttribute("cardMsg", cardEntity);
        return "member/x_member_card_edit";
    }

    @PostMapping("/activeOpt.do")
    @ResponseBody
    public R activeOpt(Long memberId, Long bindId, Integer status, HttpSession session) {
        EmployeeEntity loginUser = (EmployeeEntity) session.getAttribute("LOGIN_USER");
        String operatorName = loginUser.getName();
        try {
            memberCardService.activeOpt(memberId, bindId, status, operatorName);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/cardAdd.do")
    @ResponseBody
    @Transactional
    public R cardAdd(@Valid MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        return memberCardService.addCard(cardEntity, courseListStr, bindingResult);
    }

    @PostMapping("/cardEdit.do")
    @ResponseBody
    public R cardEdit(MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        //Bean Validation
        ValidationUtil.getErrors(bindingResult);
        try {
            memberCardService.editCard(cardEntity, courseListStr);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
        return R.ok();
    }

    @PostMapping("/toSearchByMemberId.do")
    @ResponseBody
    public R getByMemberId(Long memberId) {
        List<MemberCardEntity> cardList = memberCardService.getActiveCards(memberId);
        return R.ok().put("value", cardList);
    }

    @PostMapping("/cardTip.do")
    @ResponseBody
    public R cardTip(Long cardId, Long memberId, Long scheduleId) {
        try {
            return memberCardService.getCardTip(cardId, memberId, scheduleId);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }
}
