package com.berg.application.thirdclass.mapper;

import com.berg.application.thirdclass.dto.DepartmentDto;

import java.util.List;

/**
 * 院系实体的mapper
 */
public interface DepartmentMapper {

    List<DepartmentDto> select(DepartmentDto departmentDto);
}
