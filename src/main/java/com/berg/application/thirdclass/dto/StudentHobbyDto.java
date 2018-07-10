package com.berg.application.thirdclass.dto;

/**
 * 学生爱好映射实体
 */
public class StudentHobbyDto {

    /**
     * 映射id
     */
    private Long id;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 爱好id
     */
    private Long hobbyId;


    /**
     * 扩展属性
     */
    /**
     * 学生名称
     */
    private String studentName;

    /**
     * 爱好名称
     */
    private String hobbyName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getHobbyId() {
        return hobbyId;
    }

    public void setHobbyId(Long hobbyId) {
        this.hobbyId = hobbyId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getHobbyName() {
        return hobbyName;
    }

    public void setHobbyName(String hobbyName) {
        this.hobbyName = hobbyName;
    }

    @Override
    public String toString() {
        return "StudentHobbyDto{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", hobbyId=" + hobbyId +
                ", studentName='" + studentName + '\'' +
                ", hobbyName='" + hobbyName + '\'' +
                '}';
    }
}
