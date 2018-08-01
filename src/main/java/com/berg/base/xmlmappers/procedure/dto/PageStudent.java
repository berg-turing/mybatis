package com.berg.base.xmlmappers.procedure.dto;

import java.util.List;

public class PageStudent {

    private Integer start;

    private Integer end;

    private Integer count;

    private String studentName;

    private List<Student> studentList;

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    @Override
    public String toString() {
        return "PageStudent{" +
                "start=" + start +
                ", end=" + end +
                ", count=" + count +
                ", studentName='" + studentName + '\'' +
                ", studentList=" + studentList +
                '}';
    }
}
