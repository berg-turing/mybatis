package com.berg.application.firstclass.mapper;

import com.berg.application.firstclass.dto.DepartmentDto;

import java.util.List;

/**
 * 院系实体的mapper
 */
public interface DepartmentMapper {

    DepartmentDto selectOne(Long departmentId);

    List<DepartmentDto> select(DepartmentDto departmentDto);
}
