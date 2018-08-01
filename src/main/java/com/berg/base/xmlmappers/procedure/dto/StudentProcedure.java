package com.berg.base.xmlmappers.procedure.dto;

import java.util.Date;

/**
 * 学生对象的执行过程对象
 */
public class StudentProcedure {

    /**
     * 学生姓名
     * 输入参数
     */
    private String name;

    /**
     * 当前姓名模糊查询结果的条数
     * 输出参数
     */
    private Long count;

    /**
     * 当前sql语句的执行时间
     * 输出参数
     */
    private Date execDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Date getExecDate() {
        return execDate;
    }

    public void setExecDate(Date execDate) {
        this.execDate = execDate;
    }

    @Override
    public String toString() {
        return "StudentProcedure{" +
                "name='" + name + '\'' +
                ", count=" + count +
                ", execDate=" + execDate +
                '}';
    }
}
