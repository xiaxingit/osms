package com.one.school.controller.homework;

import com.one.school.bean.BaseResponse;
import com.one.school.bean.User;
import com.one.school.controller.BaseController;
import com.one.school.service.homework.HomeworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * Created by lee on 2019/11/3.
 */

@Slf4j
@RestController
public class HomeworkController extends BaseController {

    @Resource
    private HomeworkService homeworkService;

    @GetMapping(value = "/homework/student/info")
    public BaseResponse getStudentList(Integer pageNum, Integer pageSize) {
        try {
            return super.successResult(homeworkService.getStudentList(pageNum, pageSize, null));
        }catch (Exception ex) {
            log.error("学生信息查询失败", ex);
            return super.errorResult();
        }
    }

    @GetMapping(value = "/homework/courseware/info")
    public BaseResponse getCoursewareList(String studentId) {
        try {
            return super.successResult(homeworkService.getCoursewareList(studentId));
        }catch (Exception ex) {
            log.error("课件列表查询失败", ex);
            return super.errorResult();
        }
    }

    @GetMapping(value = "/homework/auto")
    public BaseResponse handleHomework(String studentIds) {
        try {
            homeworkService.handleHomework(studentIds);
            return super.successResult();
        }catch (Exception ex) {
            log.error("自动学习失败", ex);
            return super.errorResult();
        }
    }

    @GetMapping(value = "/homework/student/list")
    public ModelAndView goLogin() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("homework");
        mv.addObject("students", homeworkService.getStudentList(1, 5, -2));
        return mv;
    }

}
