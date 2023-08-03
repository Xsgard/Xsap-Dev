package com.kclm.xsap.exceptions;

import com.kclm.xsap.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author Asgard
 * @version 1.0
 * @description: 全局异常处理器
 * @date 2023/8/3 9:02
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 异常处理方法
     *
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] s = ex.getMessage().split(" ");
            String msg = s[2];
            return R.error(msg + "已存在！");
        }
        return R.error("未知错误！");
    }

    /**
     * 自定义业务异常处理
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public R CustomExceptionHandler(BusinessException ex) {
        log.info(ex.getMessage());
        return R.error(ex.getMessage());
    }

}
