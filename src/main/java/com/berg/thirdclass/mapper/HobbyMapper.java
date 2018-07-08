package com.berg.thirdclass.mapper;

import com.berg.thirdclass.dto.HobbyDto;

import java.util.List;

public interface HobbyMapper {

    List<HobbyDto> select(HobbyDto hobbyDto);
}
