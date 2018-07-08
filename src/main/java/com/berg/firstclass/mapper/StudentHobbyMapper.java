package com.berg.firstclass.mapper;


import com.berg.firstclass.dto.StudentHobbyDto;

import java.util.List;

public interface StudentHobbyMapper {

    StudentHobbyDto selectOne(Long studentHobbyId);

    List<StudentHobbyDto> select(StudentHobbyDto studentHobbyDto);

    List<StudentHobbyDto> selectWithExtends(StudentHobbyDto studentHobbyDto);
}
