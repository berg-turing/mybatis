package com.berg.secondclass.mapper;

import com.berg.secondclass.dto.HobbyDto;

import java.util.List;

public interface HobbyMapper {

    List<HobbyDto> select(HobbyDto hobbyDto);
}
