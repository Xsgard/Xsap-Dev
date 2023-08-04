/****************************************
 * 2018 - 2021 版权所有 CopyRight(c) 快程乐码信息科技有限公司所有, 未经授权，不得复制、转发
 */

package com.kclm.xsap.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 *
 * @author fangkai
 * @date 2022/1/2 0002 14:08
 * @return web文件上传--虚拟路径映射
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {
    private final String UPLOAD_IMAGE_URL;

    public WebMvcConfig() {
        log.debug("虚拟映射路径处理");
        String homeDir = System.getProperty("user.dir");
//        UPLOAD_IMAGE_URL = "file:" + homeDir + "\\upload\\images\\";
        UPLOAD_IMAGE_URL = "file:" + homeDir + "/upload/images/";
        log.debug("\n----> 上传的图片映射路径：{}",UPLOAD_IMAGE_URL);
    }

    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean lvfb = new LocalValidatorFactoryBean();
        //设置属性
        lvfb.setValidationMessageSource(messageSource());
        //返回
        return lvfb;
    }

    /*****
     * 用来指定验证时要读取的资源文件
     * @return
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        //属性
//        messageSource.setBasename("beanValidation");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(60);
        messageSource.setAlwaysUseMessageFormat(true);
        //
        return messageSource;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //添加一个文件上传的静态路径映射, 如果是文件目录，则需要以 file: 开头
        registry.addResourceHandler("/images/**").addResourceLocations(UPLOAD_IMAGE_URL);
    }


}
