package com.kclm.xsap.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author harima
 * @CreateDate 2020年9月17日 下午12:03:51
 * @description 此类用来描述了会员携带的各种数据
 * @since JDK11.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MemberDTO {

    private Long id;
    /**
     * 会员名称
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 手机号
     */
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joiningDate;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 备注
     */
    private String note;

    /**
     * 会员卡名
     */
    private List<String> cardNameList;
    /**
     * 会员卡信息
     */
    private List<MemberCardDTO> cardMessage;

    /**
     * 上课记录
     */
    private List<ClassRecordDTO> classRecord;

    /**
     * 预约记录
     */
    private List<ReserveRecordDTO> reserveRecord;

    /**
     * 消费记录
     */
    private List<ConsumeRecordDTO> consumeRecord;

}
