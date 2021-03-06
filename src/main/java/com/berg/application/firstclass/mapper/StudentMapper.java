package com.berg.application.firstclass.mapper;

import com.berg.application.firstclass.dto.StudentDto;

import java.util.List;

/**
 * 学生实体的mapper
 */
public interface StudentMapper {

    StudentDto selectOne(Long studentId);

    List<StudentDto> select(StudentDto studentDto);

    List<StudentDto> selectWithExtends(StudentDto studentHobbyDto);
}
