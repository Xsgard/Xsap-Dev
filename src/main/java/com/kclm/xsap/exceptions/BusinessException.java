package com.kclm.xsap.exceptions;

import lombok.Getter;

/**
 * @author Asgard
 * @version 1.0
 * @description: 自定义业务异常
 * @date 2023/8/3 8:54
 */
@Getter
public class BusinessException extends RuntimeException {
    private String msg;
    private Integer code = 500;

    public BusinessException(String message) {
        super(message);
        this.msg = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.msg = message;
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
