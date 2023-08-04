package com.kclm.xsap.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author Asgard
 * @version 1.0
 * @description: 会员卡信息DTO
 * @date 2023/8/2 15:56
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MemberCardDTO {

    /**
     * 绑定id
     */
    private Long bindId;
    /**
     * 会员卡名
     */
    private String name;
    /**
     * 卡类型
     */
    private String type;
    /**
     * 可用次数
     */
    private Integer totalCount;
    /**
     * 有效期
     */
    private Integer validDay;

    /**
     * 到期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime dueTime;
    /**
     * 激活状态
     */
    private Integer activeStatus;

    /**
     *
     */
    private LocalDateTime createTime;
    /**
     *
     */
    private LocalDateTime lastModifyTime;
}
