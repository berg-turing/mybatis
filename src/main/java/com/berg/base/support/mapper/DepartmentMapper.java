package com.berg.base.support.mapper;

import com.berg.base.support.dto.DepartmentDto;

import java.util.List;

/**
 * 院系实体的mapper
 */
public interface DepartmentMapper {

    /**
     * 根据id，查找一个院系对象
     *
     * @param departmentId      院系id
     * @return                  查找到的院系对象
     */
    DepartmentDto selectOne(Long departmentId);

    /**
     * 根据条件查询院系对象
     *
     * @param departmentDto     查询条件
     * @return                  查找到的院系对象
     */
    List<DepartmentDto> select(DepartmentDto departmentDto);
}
