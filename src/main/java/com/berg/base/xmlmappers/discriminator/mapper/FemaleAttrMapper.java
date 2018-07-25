package com.berg.base.xmlmappers.discriminator.mapper;

import com.berg.base.xmlmappers.discriminator.dto.FemaleAttr;

import java.util.List;

public interface FemaleAttrMapper {

    /**
     * 根据学生id查询女生特性
     *
     * @param studentId     学生id
     * @return              女生特性
     */
    List<FemaleAttr> fineMore(Long studentId);
}
