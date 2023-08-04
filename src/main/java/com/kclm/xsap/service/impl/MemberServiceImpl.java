package com.kclm.xsap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kclm.xsap.dao.MemberDao;
import com.kclm.xsap.dto.MemberCardDTO;
import com.kclm.xsap.dto.MemberDTO;
import com.kclm.xsap.dto.convert.MemberConvert;
import com.kclm.xsap.entity.MemberBindRecordEntity;
import com.kclm.xsap.entity.MemberCardEntity;
import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.service.MemberBindRecordService;
import com.kclm.xsap.service.MemberCardService;
import com.kclm.xsap.service.MemberService;
import com.kclm.xsap.utils.TimeCalculate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Asgard
 * @version 1.0
 * @description: MemberServiceImpl
 * @date 2023/8/2 16:43
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    private MemberCardService cardService;

    private MemberBindRecordService bindRecordService;

    @Autowired
    private void setApplicationContext(MemberCardService cardService,
                                       MemberBindRecordService bindRecordService) {
        this.cardService = cardService;
        this.bindRecordService = bindRecordService;
    }

    @Override
    public List<MemberCardDTO> cardInfo(Long id) {
        LambdaQueryWrapper<MemberBindRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null, MemberBindRecordEntity::getMemberId, id);
        List<MemberBindRecordEntity> bindRecordEntities = bindRecordService.list(queryWrapper);
        return bindRecordEntities.stream().map((item) -> {
            MemberCardEntity card = cardService.getById(item.getCardId());
            LocalDateTime endTime = TimeCalculate.timeSub(item.getCreateTime(), item.getValidDay());
            return new MemberCardDTO(
                    item.getId(), card.getName(), card.getType(),
                    item.getValidCount(), item.getValidDay(), endTime,
                    item.getActiveStatus(), null, null
            );
        }).collect(Collectors.toList());
    }

    @Override
    public List<MemberDTO> memberDtoList() {
        //查询出MemberEntity的List
        List<MemberEntity> memberEntityList = this.list();
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

    @Override
    public MemberEntity queryByPhone(String phone) {
        //查询条件，根据手机号查询数据库中是否有重复
        LambdaQueryWrapper<MemberEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotEmpty(phone), MemberEntity::getPhone, phone);
        //查询到的记录数
        return this.getOne(queryWrapper);
    }
}
