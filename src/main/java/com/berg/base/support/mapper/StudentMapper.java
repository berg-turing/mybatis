package com.berg.base.support.mapper;

import com.berg.base.support.dto.StudentDto;

import java.util.List;

/**
 * 学生实体的mapper
 */
public interface StudentMapper {

    /**
     * 查找一个学生对象
     *
     * @param studentId     学生的id
     * @return              查找到的学生对象
     */
    StudentDto selectOne(Long studentId);

    /**
     * 查找满足条件的学生对象
     *
     * @param studentDto    查询条件
     * @return              满足条件的学生对象
     */
    List<StudentDto> select(StudentDto studentDto);

    /**
     * 查找满足条件的学生对象，并关联查询
     * @param studentDto    查询条件
     * @return              满足条件的带关联信息的学生对象
     */
    List<StudentDto> selectWithExtends(StudentDto studentDto);
}
