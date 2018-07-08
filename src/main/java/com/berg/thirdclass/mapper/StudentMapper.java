package com.berg.thirdclass.mapper;

import com.berg.thirdclass.dto.StudentDto;

import java.util.List;

/**
 * 学生实体的mapper
 */
public interface StudentMapper {

    List<StudentDto> select(StudentDto studentDto);

    List<StudentDto> selectWithExtends(StudentDto studentHobbyDto);
}
