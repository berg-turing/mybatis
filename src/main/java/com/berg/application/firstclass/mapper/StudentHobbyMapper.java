package com.berg.application.firstclass.mapper;


import com.berg.application.firstclass.dto.StudentHobbyDto;

import java.util.List;

public interface StudentHobbyMapper {

    StudentHobbyDto selectOne(Long studentHobbyId);

    List<StudentHobbyDto> select(StudentHobbyDto studentHobbyDto);

    List<StudentHobbyDto> selectWithExtends(StudentHobbyDto studentHobbyDto);
}
