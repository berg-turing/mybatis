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
     * @param id            主键
     * @param maleAttr      需要更新的数据
     * @return              更新结果
     */
    int updateById(@Param("id") Long id,  MaleAttr maleAttr);
}
