package com.berg.secondclass.mapper;


import com.berg.secondclass.dto.StudentHobbyDto;

import java.util.List;

public interface StudentHobbyMapper {

    List<StudentHobbyDto> select(StudentHobbyDto studentHobbyDto);

    List<StudentHobbyDto> selectWithExtends(StudentHobbyDto studentHobbyDto);
}
