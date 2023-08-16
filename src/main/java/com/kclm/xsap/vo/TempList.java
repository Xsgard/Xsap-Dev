package com.kclm.xsap.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: TODO
 * @date 2023/8/16 9:55
 */
@Data
public class TempList {
    private List<Integer> countChanges;
    private List<Float> moneyCosts;
}
