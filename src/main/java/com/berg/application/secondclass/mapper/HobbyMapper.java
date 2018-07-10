package com.berg.application.secondclass.mapper;

import com.berg.application.secondclass.dto.HobbyDto;

import java.util.List;

public interface HobbyMapper {

    List<HobbyDto> select(HobbyDto hobbyDto);
}
