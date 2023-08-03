package com.kclm.xsap.exceptions;

/**
 * @author Asgard
 * @version 1.0
 * @description: 自定义业务异常
 * @date 2023/8/3 8:54
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
