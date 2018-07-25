package com.berg.base.xmlmappers.discriminator.mapper;

import com.berg.base.xmlmappers.discriminator.dto.BaseStudent;

import java.util.List;

public interface StudentMapper {

    /**
     * 查找学生
     *
     * @param student       查询条件
     * @return              满足条件的学生
     */
    List<BaseStudent> selectDiscriminatorBySelect(BaseStudent student);

    /**
     * 查找学生
     *
     * @param student       查询条件
     * @return              满足条件的学生
     */
    List<BaseStudent> selectDiscriminatorByResultMap(BaseStudent student);
}
