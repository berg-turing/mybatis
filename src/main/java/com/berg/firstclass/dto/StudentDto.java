package com.berg.firstclass.dto;

import java.util.Date;
import java.util.List;

/**
 * 学生实体
 */
public class StudentDto {

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
     * 院系
     */
    private DepartmentDto departmentDto;

    /**
     * 爱好
     */
    private List<HobbyDto> hobbyDtos;

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

    public DepartmentDto getDepartmentDto() {
        return departmentDto;
    }

    public void setDepartmentDto(DepartmentDto departmentDto) {
        this.departmentDto = departmentDto;
    }

    public List<HobbyDto> getHobbyDtos() {
        return hobbyDtos;
    }

    public void setHobbyDtos(List<HobbyDto> hobbyDtos) {
        this.hobbyDtos = hobbyDtos;
    }

    @Override
    public String toString() {
        return "StudentDto{" +
                "studentId=" + studentId +
                ", departmentId=" + departmentId +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", birthday=" + birthday +
                ", departmentDto=" + departmentDto +
                ", hobbyDtos=" + hobbyDtos +
                '}';
    }
}
