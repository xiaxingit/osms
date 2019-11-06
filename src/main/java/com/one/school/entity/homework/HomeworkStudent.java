package com.one.school.entity.homework;

import lombok.Data;

import java.util.Date;

@Data
public class HomeworkStudent {
    private String studentId;

    private String studentName;

    private Integer status;

    private String msg;

    private Date createTime;

    private String createTimeStr;

    private String pwd;

    private Integer isSpare;

}