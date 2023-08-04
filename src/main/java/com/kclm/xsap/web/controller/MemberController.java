package com.kclm.xsap.web.controller;

import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Asgard
 * @version 1.0
 * @description: 用于处理/member的请求
 * @date 2023/8/3 10:23
 */
@Controller
@RequestMapping("/member")
public class MemberController {
    private static final String phoneRegex = "^1[3-9]\\d{9}$";

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

    @GetMapping("/x_member_list.do")
    public String list_do() {
        return "member/x_member_list";
    }

    @GetMapping("/x_member_list_details.do")
    public String toMemberDetail(Integer id, Model model) {
        model.addAttribute("ID", id);
        return "member/x_member_list_details";
    }

    @GetMapping("/x_member_add.do")
    public String toMemberAdd() {
        return "member/x_member_add";
    }

    @PostMapping("/memberList.do")
    @ResponseBody
    public List<MemberDTO> memberEntityList() {
        return memberService.memberDtoList();
    }


    @ResponseBody
    @PostMapping("/memberDetail.do")
    public R memberDetail(Integer id) {
        MemberEntity memberEntity = memberService.getById(id);
        return new R().put("data", memberEntity);
    }

    @PostMapping("/cardInfo.do")
    @ResponseBody
    public R cardInfo(Long id) {
        List<MemberCardDTO> dtoList = memberService.cardInfo(id);
        return new R().put("data", dtoList);
    }

    @PostMapping("/memberAdd.do")
    @ResponseBody
    public R memberAdd(@Valid MemberEntity member, BindingResult bindingResult) {
        //BeanValidation校验前端传入的数据
        if (bindingResult.hasErrors()) {
            return ValidationUtil.getErrors(bindingResult);
        }
        //根据手机号查询数据库中是否有重复记录
        MemberEntity queried = memberService.queryByPhone(member.getPhone());
        //记录数大于0 或 手机号不满足正则校验
//        if (count > 0 || member.getPhone().matches(phoneRegex)) {
        if (queried != null) {
            return R.error(400, "手机号有误，请检查是否操作有误！");
        } else {
            member.setCreateTime(LocalDateTime.now());
            //保存
            memberService.save(member);
            return new R().put("data", member);
        }
    }

    @PostMapping("/x_member_edit.do")
    @ResponseBody
    public R getMemberEditInfo(Long id) {
        //
        MemberEntity memberEntity = memberService.getById(id);
        return new R().put("data", memberEntity);
    }

    @PostMapping("/memberEdit.do")
    @ResponseBody
    public R memberEdit(@Valid MemberEntity member, BindingResult bindingResult) {
        //BeanValidation校验前端传入的数据
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fe : fieldErrors) {
                System.out.println(fe.getField() + " ==> " + fe.getDefaultMessage());
            }
            return R.error().put("error", fieldErrors);
        } else {
            //修改
            boolean b = memberService.updateById(member);
            if (b)
                return R.ok();
            else
                return R.error();
        }
    }

    @PostMapping("/deleteOne.do")
    @ResponseBody
    public R deleteMember(Long id) {
        boolean b = memberService.removeById(id);
        if (b)
            return R.ok();
        else
            return R.error();
    }

}
