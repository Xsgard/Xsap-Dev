package com.kclm.xsap.utils;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Asgard
 * @version 1.0
 * @description: ValidationUtil
 * @date 2023/8/4 16:28
 */
public class ValidationUtil {
    public static R getErrors(BindingResult bindingResult) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        Map<String, String> map = new HashMap<>();
        fieldErrors.forEach((item) -> map.put(item.getField(), item.getDefaultMessage()));
        return R.error(400, "填写信息有误，请检查！").put("errorMap", map);
    }
}
