package com.one.school.service;

import com.one.school.dao.StudentMapper;
import com.one.school.entity.Student;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lee on 2019/7/21.
 */

@Service
public class StudentService {

    @Resource
    private StudentMapper studentMapper;

    public void saveStudentInfo(List<Student> studentList, Integer schoolId, String schoolName) {
        for(Student student : studentList){
            Student dbRecord = studentMapper.selectByPrimaryKey(student.getIdentityId());
            if(null == dbRecord){
                student.setSchoolId(schoolId);
                student.setSchoolName(schoolName);
                student.setPublicTimeConsuming(0);
                student.setSpecialityTimeConsuming(0);
                studentMapper.insertSelective(student);
            }
        }

    }

    public List<Student> getAllStudent() {
        return studentMapper.selectAll();
    }

}
