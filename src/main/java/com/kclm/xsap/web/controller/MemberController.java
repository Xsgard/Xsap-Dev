package com.kclm.xsap.web.controller;

import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.dto.ReserveRecordDTO;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.exceptions.BusinessException;
import com.kclm.xsap.service.ConsumeRecordService;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.vo.ClassInfoVo;
import com.kclm.xsap.vo.ConsumeInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
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
    private MemberService memberService;

    private MemberCardService cardService;

    private MemberBindRecordService bindRecordService;

    private ConsumeRecordService consumeRecordService;

    @Autowired
    private void setApplicationContext(MemberService memberService,
                                       MemberCardService cardService,
                                       MemberBindRecordService bindRecordService,
                                       ConsumeRecordService consumeRecordService) {
        this.memberService = memberService;
        this.cardService = cardService;
        this.bindRecordService = bindRecordService;
        this.consumeRecordService = consumeRecordService;
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

    @GetMapping("/toSearcherAll.do")
    @ResponseBody
    public R toSearch() {
        List<MemberEntity> list = memberService.list();
        return R.ok().put("value", list);
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
        return R.ok().put("data", memberEntity);
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
        return memberService.memberAdd(member, bindingResult);
    }

    @PostMapping("/x_member_edit.do")
    @ResponseBody
    public R getMemberEditInfo(Long id) {
        //
        MemberEntity memberEntity = memberService.getById(id);
        return new R().put("data", memberEntity);
    }

    //修改会员信息
    @PostMapping("/memberEdit.do")
    @ResponseBody
    public R memberEdit(@Valid MemberEntity member, BindingResult bindingResult) {
        try {
            return memberService.memberEdit(member, bindingResult);
        } catch (BusinessException e) {
            String msg = e.getMsg();
            return R.error(msg);
        }
    }

    //删除会员
    @PostMapping("/deleteOne.do")
    @ResponseBody
    public R deleteMember(Long id) {
        try {
            memberService.deleteMember(id);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
        return R.ok("删除成功！");
    }

    //消费记录
    @PostMapping("/consumeInfo.do")
    @ResponseBody
    public R getConsumeInfo(Long id) {
        List<ConsumeInfoVo> memberConsumeList = memberService.getMemberConsumeList(id);
        return R.ok().put("data", memberConsumeList);
    }

    //预约记录
    @PostMapping("/reserveInfo.do")
    @ResponseBody
    public R reserveInfo(Long id) {
        try {
            List<ReserveRecordDTO> reserveRecordDto = memberService.getReserveRecordDto(id);
            return R.ok().put("data", reserveRecordDto);
        } catch (BusinessException e) {
            return R.error(e.getMsg());
        }
    }

    //上课信息
    @PostMapping("/classInfo.do")
    @ResponseBody
    public R classInfo(Long id) {
        List<ClassInfoVo> classInfoList = memberService.getClassInfoList(id);

        return R.ok().put("data", classInfoList);
    }

}
