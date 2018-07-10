package com.berg.application.secondclass.dto;

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
    private StudentDto studentDto;

    private HobbyDto hobbyDto;

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

    public StudentDto getStudentDto() {
        return studentDto;
    }

    public void setStudentDto(StudentDto studentDto) {
        this.studentDto = studentDto;
    }

    public HobbyDto getHobbyDto() {
        return hobbyDto;
    }

    public void setHobbyDto(HobbyDto hobbyDto) {
        this.hobbyDto = hobbyDto;
    }

    @Override
    public String toString() {
        return "StudentHobbyDto{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", hobbyId=" + hobbyId +
                ", studentDto=" + studentDto +
                ", hobbyDto=" + hobbyDto +
                '}';
    }
}
