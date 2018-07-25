package com.berg.base.xmlmappers.associate.mapper;

import com.berg.base.xmlmappers.associate.dto.Student;

import java.util.List;

public interface StudentMapper {

    /**
     * 查找满足条件的学生对象
     *
     * @param student    查询条件
     * @return              满足条件的学生对象
     */
    List<Student> select(Student student);
}
