package com.one.school.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.Date;


@Data
public class Student extends BaseRowModel {

    @ExcelProperty(index = 0)
    private String identityId;
    @ExcelProperty(index = 1)
    private String name;

    private Integer specialityId = 4;
    @ExcelProperty(index = 3)
    private String specialityName = "行政管理";

    private Integer schoolId;
    @ExcelProperty(index = 2)
    private String schoolName;

    private Integer specialityTestNum = 0;
    @ExcelProperty(index = 6)
    private Integer specialityTestScore = 0;

    private Integer publicTestNum = 0;
    @ExcelProperty(index = 4)
    private Integer publicTestScore = 0;

    @JsonIgnore
    private Date createTime;
    @ExcelProperty(index = 8)
    private String createTimeStr;
    @ExcelProperty(index = 5)
    private Integer publicTimeConsuming;
    @ExcelProperty(index = 7)
    private Integer specialityTimeConsuming;

    private Integer isAnswer = 0;

}