package com.one.school.dao.exam;

import com.one.school.entity.exam.ExamDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamDetailMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ExamDetail record);

    int insertSelective(ExamDetail record);

    ExamDetail selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ExamDetail record);

    int updateByPrimaryKey(ExamDetail record);

    ExamDetail selectByTestPaperNumberAndStudentId(@Param(value = "testPaperNumber") String testPaperNumber,
                                                         @Param(value = "studentId") String studentId);

    List<ExamDetail> selectAll(@Param(value = "studentId") String studentId,
                               @Param(value = "studentName") String studentName);

}