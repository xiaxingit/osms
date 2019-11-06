package com.one.school.dao;

import com.one.school.entity.TestPaper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TestPaperMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(TestPaper record);

    int insertSelective(TestPaper record);

    TestPaper selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TestPaper record);

    int updateByPrimaryKey(TestPaper record);

    List<TestPaper> selectByType(@Param(value = "type") String type);
}