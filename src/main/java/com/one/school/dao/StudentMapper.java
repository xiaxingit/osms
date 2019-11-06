package com.one.school.dao;

import com.one.school.entity.Student;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StudentMapper {
    int deleteByPrimaryKey(String identityId);

    int insert(Student record);

    int insertSelective(Student record);

    Student selectByPrimaryKey(String identityId);

    int updateByPrimaryKeySelective(Student record);

    int updateByPrimaryKey(Student record);

    List<Student> selectAll();

    List<Student> incompleteStudent(@Param(value = "ids") List<String> ids);

    int updateIsAnswerValue(@Param(value = "identityId") String identityId,
                            @Param(value = "isAnswer") Integer isAnswer);

}