package com.one.school.controller.exam;

import com.one.school.bean.BaseResponse;
import com.one.school.controller.BaseController;
import com.one.school.service.exam.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lee on 2019/8/13.
 */

@Slf4j
@RestController
public class ExamController extends BaseController {

    @Resource
    private ExamService examService;

    @GetMapping(value = "/exam/template/download")
    public void downloadTemplate(HttpServletResponse response){
        examService.downloadTemplate(response);
    }

    @GetMapping(value = "/exam/identity/template/download")
    public void downloadIdentityTemplate(HttpServletResponse response){
        examService.downloadIdentityTemplate(response);
    }

    @GetMapping(value = "/exam/auth")
    public BaseResponse verStudent(String studentId, String studentPassword) {
        try {
            return super.successResult(examService.verStudent(studentId, studentPassword));
        }catch (Exception ex) {
            log.error("考生身份验证失败", ex);
            return super.errorResult();
        }
    }

    @GetMapping(value = "/exam/info")
    public BaseResponse getExamInfoByStudent(String studentId) {
        try {
            return super.successResult(examService.getExamInfoById(studentId));
        }catch (Exception ex) {
            log.error("获取考试信息失败", ex);
            return super.errorResult();
        }
    }

    @PostMapping(value = "/exam/excel")
    public BaseResponse uploadData(MultipartFile excel) {
        try {
            examService.updateData(excel, 1);
            return super.successResult();
        }catch (Exception ex) {
            log.error("考试信息导入失败", ex);
            return super.errorResult();
        }
    }

    @PostMapping(value = "/exam/identity/excel")
    public BaseResponse uploadIdentityData(MultipartFile excel) {
        try {
            examService.uploadIdentityData(excel, 1);
            return super.successResult();
        }catch (Exception ex) {
            log.error("学生信息导入失败", ex);
            return super.errorResult();
        }
    }

    @GetMapping(value = "/exam/detail/list")
    public BaseResponse getDetailList(String studentId, String studentName, Integer pageNum, Integer pageSize){
        try {
            return super.successResult(examService.getDetailList(studentId, studentName, pageNum, pageSize));
        }catch (Exception ex) {
            log.error("考试信息查询失败", ex);
            return super.errorResult();
        }
    }

    @GetMapping(value = "/exam/identity/detail/list")
    public BaseResponse getIdentityDetailList(String studentId, String studentName, Integer pageNum, Integer pageSize){
        try {
            return super.successResult(examService.getIdentityDetailList(studentId, studentName, pageNum, pageSize));
        }catch (Exception ex) {
            log.error("学生信息查询失败", ex);
            return super.errorResult();
        }
    }

}
