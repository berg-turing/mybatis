package com.berg.application.secondclass.mapper;

import com.berg.application.secondclass.dto.DepartmentDto;

import java.util.List;

/**
 * 院系实体的mapper
 */
public interface DepartmentMapper {

    List<DepartmentDto> select(DepartmentDto departmentDto);
}
