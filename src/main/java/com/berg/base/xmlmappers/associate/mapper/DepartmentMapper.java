package com.berg.base.xmlmappers.associate.mapper;

import com.berg.base.xmlmappers.associate.dto.Department;

public interface DepartmentMapper {

    /**
     * 根据id，查找一个院系对象
     *
     * @param departmentId      院系id
     * @return                  查找到的院系对象
     */
    Department selectOne(Long departmentId);
}
