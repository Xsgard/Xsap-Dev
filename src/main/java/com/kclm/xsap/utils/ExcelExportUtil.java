package com.kclm.xsap.utils;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.boot.system.ApplicationHome;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Asgard
 * @version 1.0
 * @description: excel数据导出工具类
 * @date 2023/8/23 15:31
 */
public class ExcelExportUtil {
    private static final String filePath = "/reservations/预约记录.xlsx";

    public static <T> boolean excelWriteToLocal(List<T> rows) throws IOException {
        ApplicationHome home = new ApplicationHome(ExcelExportUtil.class);
        File dir = home.getDir();
        File file = new File(dir, filePath);
        try (ExcelWriter writer = ExcelUtil.getWriter(file)) {
            writer.addHeaderAlias("courseName", "课程名");
            writer.addHeaderAlias("reserveTime", "预约时间");
            writer.addHeaderAlias("memberName", "会员名");
            writer.addHeaderAlias("cardName", "会员卡名");
            writer.addHeaderAlias("reserveNumbers", "单次预约人数");
            writer.addHeaderAlias("timesCost", "课消次数");
            writer.addHeaderAlias("operateTime", "操作时间");
            writer.addHeaderAlias("operator", "操作人");
            writer.addHeaderAlias("reserveNote", "预约备注");
            writer.addHeaderAlias("reserveStatus", "预约状态");
            // 默认的，未添加alias的属性也会写出，如果想只写出加了别名的字段，可以调用此方法排除之
            writer.setOnlyAlias(true);
            writer.write(rows, true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> void excelWriteToOnline(List<T> rows, HttpServletResponse response) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        try (ExcelWriter writer = ExcelUtil.getWriter(file)) {
            writer.addHeaderAlias("courseName", "课程名");
            writer.addHeaderAlias("reserveTime", "预约时间");
            writer.addHeaderAlias("memberName", "会员名");
            writer.addHeaderAlias("cardName", "会员卡名");
            writer.addHeaderAlias("reserveNumbers", "单次预约人数");
            writer.addHeaderAlias("timesCost", "课消次数");
            writer.addHeaderAlias("operateTime", "操作时间");
            writer.addHeaderAlias("operator", "操作人");
            writer.addHeaderAlias("reserveNote", "预约备注");
            writer.addHeaderAlias("reserveStatus", "预约状态");

            writer.write(rows, true);
            ServletOutputStream out = response.getOutputStream();
            String fileName = URLEncoder.encode("预约记录", "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            writer.flush(out, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
