package com.one.school.service;

import com.one.school.entity.Student;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Callable;

/**
 * Created by lee on 2019/7/23.
 */

@Slf4j
public class TestPaperCallable implements Callable<Integer> {

    private TestPaperService testPaperService;

    private Student student;

    public TestPaperCallable(TestPaperService testPaperService, Student student){
        this.testPaperService = testPaperService;
        this.student = student;
    }

    @Override
    public Integer call() throws Exception {
        try {
            testPaperService.startTestPaper(student);
        }catch (Exception ex){
            log.error(ex.getMessage(), ex);
        }
        return null;
    }
}
