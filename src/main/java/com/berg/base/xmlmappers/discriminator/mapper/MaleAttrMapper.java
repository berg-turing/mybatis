package com.berg.base.xmlmappers.discriminator.mapper;

import com.berg.base.xmlmappers.discriminator.dto.MaleAttr;

import java.util.List;

public interface MaleAttrMapper {

    /**
     * 根据学生id查询男生特性
     *
     * @param studentId     学生id
     * @return              男生特性
     */
    List<MaleAttr> fineMore(Long studentId);
}
