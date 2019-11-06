package com.one.school.util;

import com.alibaba.fastjson.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by lee on 2019/7/27.
 */

@Slf4j
public class FileUtil {

    /**
     * 文件下载
     * @param response  Servlet
     * @param filePath  文件存放路径
     * @param fileName  文件名
     */
    public static void download(HttpServletResponse response, String filePath, String fileName) {
        long startTime = System.currentTimeMillis();
        if(StringUtils.isEmpty(filePath)){
            log.error("File path is not empty");
            throw new RuntimeException("文件路径参数异常");
        }
        if(StringUtils.isEmpty(fileName)){
            log.error("File name is not empty");
            throw new RuntimeException("文件名参数异常");
        }
        String fullPath = String.format("%s%s", filePath, fileName);
        File file = new File(fullPath);
        if(!file.exists()){
            log.error("File not found , [filePath]:{} , [fileName]:{}", filePath, fileName);
            throw new RuntimeException("文件未找到异常");
        }
        if(log.isDebugEnabled()){
            log.debug("File found in path, [fullPath]:{}", fullPath);
        }
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        ServletOutputStream out = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            out = response.getOutputStream();
            int b = 0;
            byte[] buffer = new byte[512];
            while (b != -1){
                b = inputStream.read(buffer);
                out.write(buffer,0,b);
            }
            if(log.isDebugEnabled()){
                log.debug("File download takes {} ms", System.currentTimeMillis() - startTime);
            }
        } catch (Exception ex) {
            log.error("Exception in file download", ex);
        }finally {
            IOUtils.close(inputStream);
            IOUtils.close(out);
        }
    }

}
