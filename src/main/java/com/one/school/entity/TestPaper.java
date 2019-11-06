package com.one.school.entity;

import lombok.Data;

@Data
public class TestPaper {
    private Integer id;

    private String questionCode;

    private String rightAnswersCode;

    private String wrongAnswersCode;

    private String questionType;

    private Integer questionScore;

    private String testPaperType;

    private String testPaperClass;

}