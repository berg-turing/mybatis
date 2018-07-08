package com.berg.thirdclass.dto;

/**
 * 院系实体
 */
public class DepartmentDto {

    /**
     * 院系id
     */
    private Long departmentId;

    /**
     * 院系名称
     */
    private String name;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DepartmentDto{" +
                "departmentId=" + departmentId +
                ", name='" + name + '\'' +
                '}';
    }
}
