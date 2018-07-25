package com.berg.base.xmlmappers.insert.mapper;

import com.berg.base.xmlmappers.insert.dto.MaleAttr;

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
     * 插入数据，不带反写id
     *
     * @param maleAttr      需要插入的数据
     * @return              插入的结果，影响的条数
     */
    int insert(MaleAttr maleAttr);

    /**
     * 插入数据，带反写id
     *
     * @param maleAttr      需要插入的数据
     * @return              插入的结果，影响的条数
     */
    int insertBack(MaleAttr maleAttr);

    /**
     * 选择插入数据，不带反写id
     *
     * @param maleAttr      需要插入的数据
     * @return              插入的结果，影响的条数
     */
    int insertSelect(MaleAttr maleAttr);

    /**
     * 选择插入数据，带反写id
     *
     * @param maleAttr      需要插入的数据
     * @return              插入的结果，影响的条数
     */
    int insertSelectBack(MaleAttr maleAttr);

    /**
     * 使用selectKey标签，选择插入数据，带反写id
     *
     * @param maleAttr      需要插入的数据
     * @return              插入的结果，影响的条数
     */
    int insertSelectBackUseSelectKey(MaleAttr maleAttr);
}
