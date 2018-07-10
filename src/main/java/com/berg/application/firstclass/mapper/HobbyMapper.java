package com.berg.application.firstclass.mapper;

import com.berg.application.firstclass.dto.HobbyDto;

import java.util.List;

public interface HobbyMapper {

    HobbyDto selectOne(Long hobbyId);

    List<HobbyDto> selectMore(Long studentId);

    List<HobbyDto> select(HobbyDto hobbyDto);
}
