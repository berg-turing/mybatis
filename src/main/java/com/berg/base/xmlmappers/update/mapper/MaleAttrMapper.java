package com.berg.base.xmlmappers.update.mapper;

import com.berg.base.xmlmappers.update.dto.MaleAttr;
import org.apache.ibatis.annotations.Param;

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
     * 更新数据
     *
     * @param maleAttr      需要更新的数据
     * @return              更新结果
     */
    int updateById(MaleAttr maleAttr);

    /**
     * 条件更新
     *
     * @param maleAttr      更新到的数据
     * @param condition     更新的查询条件
     * @return              影响结果
     */
    int updateByCondition(@Param("maleAttr") MaleAttr maleAttr, @Param("condition") MaleAttr condition);
}
