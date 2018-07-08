package com.berg.thirdclass.mapper;

import com.berg.thirdclass.dto.DepartmentDto;

import java.util.List;

/**
 * 院系实体的mapper
 */
public interface DepartmentMapper {

    List<DepartmentDto> select(DepartmentDto departmentDto);
}
