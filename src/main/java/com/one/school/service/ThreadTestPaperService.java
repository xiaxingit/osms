package com.one.school.service;

import com.one.school.dao.StudentMapper;
import com.one.school.entity.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by lee on 2019/7/23.
 */

@Slf4j
@Service
public class ThreadTestPaperService {

    @Resource
    private StudentMapper studentMapper;
    @Resource
    private TestPaperService testPaperService;

    public String startTestPaper(String[] ids){
        List<String> list = null == ids ? null : Arrays.asList(ids);
        List<Student> students = studentMapper.incompleteStudent(list);
        ThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
        for(Student student : students){
//            studentMapper.updateIsAnswerValue(student.getIdentityId(), 1);
            TestPaperCallable callable = new TestPaperCallable(testPaperService, student);
            executor.submit(callable);
        }
        executor.shutdown();
        int size = students.size();
        int min = size < 5 ? 14 : (size/5+1)*14;
        return String.format("%s 位学生正在答题,预计总耗时 %s 分钟,请稍后点击【刷新结果】按钮获取考试结果", size, min);
    }

}
