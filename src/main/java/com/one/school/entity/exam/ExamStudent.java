package com.one.school.entity.exam;

import java.util.Date;
import java.util.List;

public class ExamStudent {
    private String studentId;

    private String studentName;

    private String studentPassword;

    private String examName;

    private String gradeSeason;

    private String studentCategory;

    private String major;

    private Date createTime;

    private Integer createUserId;

    private List<ExamDetail> details;

    public List<ExamDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ExamDetail> details) {
        this.details = details;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId == null ? null : studentId.trim();
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName == null ? null : studentName.trim();
    }

    public String getStudentPassword() {
        return studentPassword;
    }

    public void setStudentPassword(String studentPassword) {
        this.studentPassword = studentPassword == null ? null : studentPassword.trim();
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName == null ? null : examName.trim();
    }

    public String getGradeSeason() {
        return gradeSeason;
    }

    public void setGradeSeason(String gradeSeason) {
        this.gradeSeason = gradeSeason == null ? null : gradeSeason.trim();
    }

    public String getStudentCategory() {
        return studentCategory;
    }

    public void setStudentCategory(String studentCategory) {
        this.studentCategory = studentCategory;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major == null ? null : major.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }
}