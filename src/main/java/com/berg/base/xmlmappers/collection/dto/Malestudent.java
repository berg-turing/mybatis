package com.berg.base.xmlmappers.collection.dto;

import java.util.Date;
import java.util.List;

public class Malestudent {

    /**
     * 学生id
     */

    private Long studentId;

    /**
     * 部门id
     */
    private Long departmentId;

    /**
     * 学号
     */
    private String number;

    /**
     * 姓名
     */
    private String name;

    /**
     * 生日
     */
    private Date birthday;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 男生的特性
     */
    private List<MaleAttr> maleAttrList;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public List<MaleAttr> getMaleAttrList() {
        return maleAttrList;
    }

    public void setMaleAttrList(List<MaleAttr> maleAttrList) {
        this.maleAttrList = maleAttrList;
    }

    @Override
    public String toString() {
        return "Malestudent{" +
                "studentId=" + studentId +
                ", departmentId=" + departmentId +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", sex=" + sex +
                ", maleAttrList=" + maleAttrList +
                '}';
    }
}
