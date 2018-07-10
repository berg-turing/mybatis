package com.berg.application.secondclass.mapper;


import com.berg.application.secondclass.dto.StudentHobbyDto;

import java.util.List;

public interface StudentHobbyMapper {

    List<StudentHobbyDto> select(StudentHobbyDto studentHobbyDto);

    List<StudentHobbyDto> selectWithExtends(StudentHobbyDto studentHobbyDto);
}
