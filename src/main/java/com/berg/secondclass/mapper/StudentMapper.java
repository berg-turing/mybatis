package com.berg.secondclass.mapper;

import com.berg.secondclass.dto.StudentDto;

import java.util.List;

/**
 * 学生实体的mapper
 */
public interface StudentMapper {

    List<StudentDto> select(StudentDto studentDto);

    List<StudentDto> selectWithExtends(StudentDto studentHobbyDto);
}
