package com.one.school.entity.exam;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

import java.util.Date;

@Data
public class ExamDetail extends BaseRowModel {

    private Integer id;

    @ExcelProperty(index = 0)
    private String testPaperNumber;

    @ExcelProperty(index = 1)
    private String testPaperName;

    @ExcelProperty(index = 2)
    private String studentId;

    @ExcelProperty(index = 3)
    private String studentName;

    @ExcelProperty(index = 4)
    private String gradeSeason;

    @ExcelProperty(index = 5)
    private String major;

    @ExcelProperty(index = 6)
    private String studentType;

    @ExcelProperty(index = 7)
    private String studentCategory;

    @ExcelProperty(index = 8)
    private String classCode;

    @ExcelProperty(index = 9)
    private String className;

    @ExcelProperty(index = 10)
    private String instructor;

    @ExcelProperty(index = 11)
    private String branchCode;

    @ExcelProperty(index = 12)
    private String branchName;

    @ExcelProperty(index = 13)
    private String seatNumber;

    @ExcelProperty(index = 14)
    private String examRoom;

    @ExcelProperty(index = 15)
    private String examName;

    @ExcelProperty(index = 16)
    private String examAddress;

    private String examDate;

    @ExcelProperty(index = 17)
    private Date excelExamDate;

    @ExcelProperty(index = 18)
    private String examTime;

    @ExcelProperty(index = 19)
    private String assessmentType;

    @ExcelProperty(index = 20)
    private String examType;

    @ExcelProperty(index = 21)
    private String isExamStay;

    @ExcelProperty(index = 22)
    private String isOwnDistribution;

    @ExcelProperty(index = 23)
    private String examSource;

    @ExcelProperty(index = 24)
    private String source;

    private Date createTime;

    private Integer createUserId;

    private Date updateTime;

    private Integer updateUserId;

}