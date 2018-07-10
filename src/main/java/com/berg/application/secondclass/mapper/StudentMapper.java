package com.berg.application.secondclass.mapper;

import com.berg.application.secondclass.dto.StudentDto;

import java.util.List;

/**
 * 学生实体的mapper
 */
public interface StudentMapper {

    List<StudentDto> select(StudentDto studentDto);

    List<StudentDto> selectWithExtends(StudentDto studentHobbyDto);
}
