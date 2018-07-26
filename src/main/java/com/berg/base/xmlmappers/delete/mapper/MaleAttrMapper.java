package com.berg.base.xmlmappers.delete.mapper;

import com.berg.base.xmlmappers.delete.dto.MaleAttr;

import java.util.List;

public interface MaleAttrMapper {

    /**
     * 条件查询数据
     *
     * @param maleAttr      查询条件
     * @return              查询结果
     */
    List<MaleAttr> select(MaleAttr maleAttr);

    /**
     * 通过id删除数据
     *
     * @param id        id
     * @return          删除影响的条数
     */
    int deleteById(Long id);

    /**
     * 根据条件删除数据
     *
     * @param maleAttr      删除数据的条件
     * @return              删除影响的条数
     */
    int deleteByCondition(MaleAttr maleAttr);
}
