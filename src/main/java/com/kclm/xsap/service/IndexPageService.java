package com.kclm.xsap.service;

import com.kclm.xsap.entity.MemberEntity;
import com.kclm.xsap.vo.indexStatistics.IndexAddAndStreamInfoVo;
import com.kclm.xsap.vo.indexStatistics.IndexPieChartVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/12 10:12
 */
public interface IndexPageService {
    IndexAddAndStreamInfoVo getAddAndStreamInfo(List<MemberEntity> memberList, LocalDateTime start, LocalDateTime end);

    List<IndexPieChartVo> getMemberCards();


}
