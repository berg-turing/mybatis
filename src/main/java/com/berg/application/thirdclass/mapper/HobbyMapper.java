package com.berg.application.thirdclass.mapper;

import com.berg.application.thirdclass.dto.HobbyDto;

import java.util.List;

public interface HobbyMapper {

    List<HobbyDto> select(HobbyDto hobbyDto);
}
