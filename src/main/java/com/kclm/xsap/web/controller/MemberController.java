package com.kclm.xsap.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.dto.convert.MemberConvert;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.R;
import com.kclm.xsap.utils.TimeCalculate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @PostMapping("/memberList.do")
    @ResponseBody
    public List<MemberDTO> memberEntityList() {
        //查询出MemberEntity的List
        List<MemberEntity> memberEntityList = memberService.list();
        //使用Stream流操作memberEntityList返回List<MemberDTO>类型
        return memberEntityList.stream().map((item) -> {
            //将MemberEntity转换为MemberDTO
            MemberDTO memberDTO = MemberConvert.INSTANCE.entity2Dto(item);
            memberDTO.setName(item.getName() + "(" + item.getPhone() + ")");
            memberDTO.setJoiningDate(item.getCreateTime());
            //查询条件--> 会员绑定表的memberId等于会员表的id
            LambdaQueryWrapper<MemberBindRecordEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MemberBindRecordEntity::getMemberId, item.getId());
            //查询
            List<MemberBindRecordEntity> recordEntityList = bindRecordService.list(wrapper);
            //存储会员卡名的集合
            List<String> cardName = new ArrayList<>();
            //遍历record集合
            recordEntityList.forEach((record) -> {
                if (Objects.equals(record.getMemberId(), item.getId())) {
                    //查询条件--> 会员卡表的id等于会员绑定记录表的卡号
                    LambdaQueryWrapper<MemberCardEntity> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.eq(MemberCardEntity::getId, record.getCardId());
                    List<MemberCardEntity> cardEntityList = cardService.list(queryWrapper);
                    cardEntityList.forEach((card) -> {
                        cardName.add(card.getName());
                        memberDTO.setCardNameList(cardName);
                    });
                }
            });
            return memberDTO;
        }).collect(Collectors.toList());
    }

    @GetMapping("/x_member_list_details.do")
    public String toMemberDetail(Integer id, Model model) {
        model.addAttribute("ID", id);
        return "member/x_member_list_details";
    }

    @ResponseBody
    @PostMapping("/memberDetail.do")
    public R memberDetail(Integer id) {
        MemberEntity memberEntity = memberService.getById(id);
        return new R().put("data", memberEntity);
    }

    @PostMapping("/cardInfo.do")
    @ResponseBody
    public R cardInfo(Integer id) {
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null, MemberBindRecordEntity::getMemberId, id);
        List<MemberBindRecordEntity> bindRecordEntities = bindRecordService.list(queryWrapper);
        List<MemberCardDTO> dtoList = bindRecordEntities.stream().map((item) -> {
            MemberCardEntity card = cardService.getById(item.getCardId());
            LocalDateTime endTime = TimeCalculate.timeSub(item.getCreateTime(), item.getValidDay());
            return new MemberCardDTO(
                    item.getId(), card.getName(), card.getType(),
                    item.getValidCount(), item.getValidDay(), endTime,
                    item.getActiveStatus(), null, null
            );
        }).collect(Collectors.toList());
        return new R().put("data", dtoList);
    }

}
