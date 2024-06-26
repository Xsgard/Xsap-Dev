package com.kclm.xsap.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Asgard
 * @version 1.0
 * @description: 上传图片
 * @date 2023/8/14 19:09
 */
@Slf4j
public class UploadImg {
    private UploadImg() {
    }

    public static String uploadImg(MultipartFile avatarFile, String savePath) {
        String filename = avatarFile.getOriginalFilename();//获取文件名以及后缀名
        String projectPath = System.getProperty("user.dir");
        String dirPath = projectPath + savePath;
        log.info(dirPath);
        File filePath = new File(dirPath);
        if (!filePath.exists())
            filePath.mkdirs();
        UUID uuid = UUID.randomUUID();//生成文件名
        String newFileName = null;//生成文件名+后缀
        if (filename != null) {
            newFileName = uuid + filename.substring(filename.lastIndexOf("."));
        }
        try {
            log.info(dirPath + newFileName);
            avatarFile.transferTo(new File(dirPath + newFileName));
        } catch (IOException e) {
            log.debug(e.getMessage());
            return null;
        }
        return newFileName;
    }
}
