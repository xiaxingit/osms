package com.one.school.entity.homework;

public class HomeworkCourseware {
    private String studentId;

    private String coursewareName;

    private String coursewareType;

    private Integer isDone;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId == null ? null : studentId.trim();
    }

    public String getCoursewareName() {
        return coursewareName;
    }

    public void setCoursewareName(String coursewareName) {
        this.coursewareName = coursewareName == null ? null : coursewareName.trim();
    }

    public String getCoursewareType() {
        return coursewareType;
    }

    public void setCoursewareType(String coursewareType) {
        this.coursewareType = coursewareType == null ? null : coursewareType.trim();
    }

    public Integer getIsDone() {
        return isDone;
    }

    public void setIsDone(Integer isDone) {
        this.isDone = isDone;
    }
}