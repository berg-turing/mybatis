package com.berg.base.xmlmappers.insert.dto;

public class MaleAttr {

    /**
     * id
     */
    private Long id;

    /**
     * 学生id
     */
    private Long studentId;

    /**
     * 游戏
     */
    private String game;

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

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    @Override
    public String toString() {
        return "MaleAttr{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", game='" + game + '\'' +
                '}';
    }
}
