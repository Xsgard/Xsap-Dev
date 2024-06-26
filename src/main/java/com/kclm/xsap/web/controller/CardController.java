package com.kclm.xsap.web.controller;

import com.kclm.xsap.entity.EmployeeEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.entity.RechargeRecordEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.CourseCardService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.RechargeRecordService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import com.kclm.xsap.vo.ConsumeFormVo;
import com.kclm.xsap.vo.OperateRecordVo;
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
import java.util.Arrays;
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
    private MemberCardService memberCardService;
    private CourseCardService courseCardService;
    private RechargeRecordService rechargeRecordService;

    @Autowired
    private void setApplicationContext(MemberCardService cardService,
                                       CourseCardService courseCardService,
                                       RechargeRecordService rechargeRecordService) {

        this.rechargeRecordService = rechargeRecordService;
        this.memberCardService = cardService;
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

    //激活、禁用会员卡
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

    //添加会员卡
    @PostMapping("/cardAdd.do")
    @ResponseBody
    @Transactional
    public R cardAdd(@Valid MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ValidationUtil.getErrors(bindingResult);
        courseListStr = Arrays.stream(courseListStr)
                .filter(item -> item != -1)//过滤为-1的值
                .toArray(Long[]::new);
        try {
            memberCardService.addCard(cardEntity, courseListStr, bindingResult);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("添加成功！");
    }

    //修改会员卡
    @PostMapping("/cardEdit.do")
    @ResponseBody
    public R cardEdit(MemberCardEntity cardEntity, Long[] courseListStr, BindingResult bindingResult) {
        //Bean Validation
        if (bindingResult.hasErrors())
            return ValidationUtil.getErrors(bindingResult);
        courseListStr = Arrays.stream(courseListStr)
                .filter(item -> item != -1) //过滤值为-1的
                .toArray(Long[]::new);
        try {
            memberCardService.editCard(cardEntity, courseListStr);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
        return R.ok("修改成功！");
    }

    //获取会员可用的会员卡
    @PostMapping("/toSearchByMemberId.do")
    @ResponseBody
    public R getByMemberId(Long memberId) {
        List<MemberCardEntity> cardList = memberCardService.getActiveCards(memberId);
        return R.ok().put("value", cardList);
    }

    //获取会员卡信息
    @PostMapping("/cardTip.do")
    @ResponseBody
    public R cardTip(Long cardId, Long memberId, Long scheduleId) {
        try {
            return memberCardService.getCardTip(cardId, memberId, scheduleId);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    //获取该卡的可使用剩余次数 和 课程中每次消费多少课次
    @PostMapping("/operateRecord.do")
    @ResponseBody
    public R operateRecord(Long memberId, Long cardId) {
        List<OperateRecordVo> operateRecords = memberCardService.getOperateRecords(memberId, cardId);
        return R.ok().put("data", operateRecords);
    }

    //充值会员卡
    @PostMapping("/rechargeOpt.do")
    @ResponseBody
    public R rechargeOpt(@Valid RechargeRecordEntity rechargeRecord, BindingResult bindingResult) {
        //BeanValidation
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        try {
            rechargeRecordService.memberRecharge(rechargeRecord);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("充值成功！");
    }

    //删除会员卡
    @PostMapping("/deleteOne.do")
    @ResponseBody
    public R deleteOne(Long id) {
        try {
            memberCardService.deleteOneCard(id);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("删除会员卡成功！");
    }

    //会员卡扣费
    @PostMapping("/consumeOpt.do")
    @ResponseBody
    public R consumeOpt(@Valid ConsumeFormVo vo, BindingResult bindingResult) {
        //BeanValidation
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        try {
            memberCardService.consumeOpt(vo);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("扣费成功！");
    }
}
