package com.berg.application.thirdclass.mapper;


import com.berg.application.thirdclass.dto.StudentHobbyDto;

import java.util.List;

public interface StudentHobbyMapper {

    List<StudentHobbyDto> select(StudentHobbyDto studentHobbyDto);

    List<StudentHobbyDto> selectWithExtends(StudentHobbyDto studentHobbyDto);
}
