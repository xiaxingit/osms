package com.one.school.dao.homework;

import com.one.school.entity.homework.HomeworkCourseware;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HomeworkCoursewareMapper {
    int insert(HomeworkCourseware record);

    int insertSelective(HomeworkCourseware record);

    List<HomeworkCourseware> selectByStudentId(@Param(value = "studentId") String studentId);

    HomeworkCourseware selectByStudentIdAndCourseName(@Param(value = "studentId") String studentId,
                                                      @Param(value = "coursewareName") String coursewareName);

    int updateStatus(@Param(value = "studentId") String studentId,
                     @Param(value = "coursewareName") String coursewareName,
                     @Param(value = "isDone") Integer isDone);
}