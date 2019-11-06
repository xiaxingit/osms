package com.one.school.dao.homework;

import com.one.school.entity.homework.HomeworkStudent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HomeworkStudentMapper {
    int insert(HomeworkStudent record);

    int insertSelective(HomeworkStudent record);

    HomeworkStudent selectByStudentId(@Param(value = "studentId") String studentId);

    List<HomeworkStudent> selectAll(@Param(value = "status") Integer status);

    int updateInfo(HomeworkStudent student);

    int updateStatus(@Param(value = "array") String[] array);

}