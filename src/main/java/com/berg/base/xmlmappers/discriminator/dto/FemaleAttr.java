package com.berg.base.xmlmappers.discriminator.dto;

public class FemaleAttr {

    /**
     * id
     */
    private Long id;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 化妆品
     */
    private String cosmetics;

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

    public String getCosmetics() {
        return cosmetics;
    }

    public void setCosmetics(String cosmetics) {
        this.cosmetics = cosmetics;
    }

    @Override
    public String toString() {
        return "FemaleAttr{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", cosmetics='" + cosmetics + '\'' +
                '}';
    }
}
