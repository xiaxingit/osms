package com.one.school.controller;

import com.one.school.bean.BaseResponse;
import com.one.school.bean.User;
import com.one.school.entity.Student;
import com.one.school.service.StudentService;
import com.one.school.service.TestPaperService;
import com.one.school.service.ThreadTestPaperService;
import com.one.school.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by lee on 2019/7/19.
 */

@Slf4j
@RestController
public class TestPaperController {

    @Resource
    private StudentService studentService;
    @Resource
    private TestPaperService testPaperService;
    @Resource
    private ThreadTestPaperService threadTestPaperService;

    private List<String> excelHead = Arrays.asList("姓名","身份证","报读分校","专业","公共基础课得分","耗时(分)","专业基础课得分","耗时(分)","答题时间");

    @GetMapping(value = "/login")
    public ModelAndView goLogin() {
        return new ModelAndView("login");
    }

    @PostMapping(value = "/login")
    public ModelAndView login(User user){
        ModelAndView mv = new ModelAndView();
        if(null == user){
            mv.setViewName("login");
            mv.addObject("loginMsg", "用户名密码不能为空");
            return mv;
        }
        if(!user.getUserName().equals("壹号学堂") || !user.getUserPwd().equals("xy123456")){
            mv.setViewName("login");
            mv.addObject("loginMsg", "用户名或密码不正确,请重新输入");
            return mv;
        }
        mv.setViewName("testPaper");
        mv.addObject("students", studentService.getAllStudent());
        return mv;
    }

    @GetMapping(value = "/paper")
    public ModelAndView getById(String ids) {
        ModelAndView modelAndView = new ModelAndView("testPaper");
        List<Student> students = studentService.getAllStudent();
        modelAndView.addObject("students", students);
        modelAndView.addObject("ids", ids);
        return modelAndView;
    }

    @PostMapping(value = "/paper/file/upload")
    public BaseResponse uploadFile(MultipartFile file, Student student) {
        BaseResponse response = new BaseResponse();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            List<Student> excelData = ExcelUtil.readExcel(file, Student.class);
            studentService.saveStudentInfo(excelData, student.getSchoolId(), student.getSchoolName());
            for(Student s : excelData){
                testPaperService.initTestPaperResult(s.getIdentityId(), s.getName(), student.getSchoolId());
                stringBuilder.append(s.getIdentityId()+",");
            }
            response.setCode(1);
            response.setData(stringBuilder.toString());
        }catch (Exception ex) {
            response.setCode(-1);
            response.setMsg("系统异常，请联系开发人员");
            log.error(ex.getMessage(), ex);
        }
        return response;
    }

    @GetMapping(value = "/paper/file/download")
    public void download(HttpServletResponse response) {
        List<Student> data = studentService.getAllStudent();
        ExcelUtil.writeExcel(response, excelHead, "test_paper_result", data, Student.class);
    }

    @GetMapping(value = "/paper/start")
    public BaseResponse startTestPaper(String[] ids) {
        BaseResponse response = new BaseResponse();
        try {
            response.setCode(1);
            response.setMsg(threadTestPaperService.startTestPaper(ids));
        }catch (Exception ex) {
            response.setCode(-1);
            response.setMsg("系统异常，请联系开发人员");
            log.error(ex.getMessage(), ex);
        }
        return response;
    }

}
