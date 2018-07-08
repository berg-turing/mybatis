package com.berg.secondclass.dto;

/**
 * 爱好的实体
 */
public class HobbyDto {

    /**
     * 爱好id
     */
    private Long hobbyId;

    /**
     * 爱好名称
     */
    private String name;

    public Long getHobbyId() {
        return hobbyId;
    }

    public void setHobbyId(Long hobbyId) {
        this.hobbyId = hobbyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "HobbyDto{" +
                "hobbyId=" + hobbyId +
                ", name='" + name + '\'' +
                '}';
    }
}
