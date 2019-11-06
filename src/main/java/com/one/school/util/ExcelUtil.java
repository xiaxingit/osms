package com.one.school.util;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.google.common.collect.Lists;
import com.one.school.listener.ExcelListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;
import java.util.List;

/**
 * Created by lee on 2019/3/15.
 */

@Slf4j
public class ExcelUtil {

    private static int HEAD_COLUMN_SIZE = 1;

    /**
     * 导出Excel
     */
    public static void writeExcel(HttpServletResponse response, List<String> headNameList, String fileName,
                                  List<? extends BaseRowModel> data, Class clazz) {
        OutputStream out = null;
        fileName = String.format("%s.%s", fileName, "xlsx");
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            response.setCharacterEncoding("UTF-8");
            List<List<String>> subList = Lists.partition(headNameList, HEAD_COLUMN_SIZE);
            Sheet sheet1 = new Sheet(1, 0,clazz);
            sheet1.setSheetName("Sheet1");
            sheet1.setHead(subList);
            out = response.getOutputStream();
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX);
            writer.write(data, sheet1);
            writer.finish();
        }catch (Exception ex) {

        }finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 默认读取第一个sheet页，并从数据第二行开始读取数据，适用于第一行作为表头的Excel文档
     * @param excel
     * @param clazz
     * @param <T>
     * @return
     */
    public static<T> List<T> readExcel(MultipartFile excel, Class clazz) {
        return readExcel(excel, clazz, 1);
    }

    /**
     * 读取指定sheet页，默认从数据第二行开始读取数据，适用于第一行作为表头的Excel文档
     * @param excel
     * @param clazz
     * @param sheetNo
     * @param <T>
     * @return
     */
    public static<T> List<T> readExcel(MultipartFile excel, Class clazz, int sheetNo) {
        return readExcel(excel, clazz, sheetNo, 1);
    }

    /**
     * 读取指定sheet页，指定数据行的Excel文档
     * @param excel
     * @param clazz
     * @param sheetNo
     * @param headLineNum
     * @param <T>
     * @return
     */
    public static<T> List<T> readExcel(MultipartFile excel, Class clazz, int sheetNo, int headLineNum) {
        if(null == excel){
            log.warn("Excel is null");
            return Collections.emptyList();
        }
        try {
            return readExcel(excel.getInputStream(), clazz, excel.getOriginalFilename(), sheetNo, headLineNum);
        }catch (Exception ex) {
            log.error("Read excel exception", ex);
            throw new RuntimeException("Excel文件解析异常");
        }
    }

    private static<T> List<T> readExcel(InputStream in, Class clazz, String fileName, int sheetNo, int headLineNum) throws IOException {
        checkExcelType(fileName);
        ExcelListener excelListener = new ExcelListener();
        ExcelReader reader = getReader(in, excelListener);
        Sheet sheet = new Sheet(sheetNo, headLineNum, clazz);
        reader.read(sheet);
        return excelListener.getData();
    }

    private static ExcelReader getReader(InputStream in, ExcelListener excelListener) throws IOException {
        InputStream inputStream = new BufferedInputStream(in);
        return new ExcelReader(inputStream, null, excelListener, false);
    }

    /**
     * 校验Excel文件后缀是否符合要求
     * @param fileName 文件名
     */
    private static void checkExcelType(String fileName) {
        fileName = null == fileName ? "" : fileName.toLowerCase();
        if(!(fileName.endsWith(ExcelTypeEnum.XLS.getValue()) || fileName.endsWith(ExcelTypeEnum.XLSX.getValue()))){
            log.error("Excel type is error, fullName : {}", fileName);
            throw new RuntimeException("请使用Excel进行文件上传");
        }
    }

}
