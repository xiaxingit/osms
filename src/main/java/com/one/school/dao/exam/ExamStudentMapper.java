package com.one.school.dao.exam;

import com.one.school.entity.exam.ExamStudent;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamStudentMapper {
    int deleteByPrimaryKey(String studentId);

    int insert(ExamStudent record);

    int insertSelective(ExamStudent record);

    ExamStudent selectByPrimaryKey(String studentId);

    int updateByPrimaryKeySelective(ExamStudent record);

    int updateByPrimaryKey(ExamStudent record);

    ExamStudent selectStudentAndDetailByKey(String studentId);

    List<ExamStudent> selectAll(@Param(value = "studentId") String studentId,
                                @Param(value = "studentName") String studentName);
}