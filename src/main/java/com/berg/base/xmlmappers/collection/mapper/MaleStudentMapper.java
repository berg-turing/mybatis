package com.berg.base.xmlmappers.collection.mapper;

import com.berg.base.xmlmappers.collection.dto.Malestudent;

import java.util.List;

public interface MaleStudentMapper {

    /**
     * 查找满足条件的学生对象
     *
     * @param malestudent   查询条件
     * @return              满足条件的学生对象
     */
    List<Malestudent> selectCollectionBySelect(Malestudent malestudent);

    /**
     * 查找满足条件的学生对象
     *
     * @param malestudent   查询条件
     * @return              满足条件的学生对象
     */
    List<Malestudent> selectCollectionByResultMap(Malestudent malestudent);
}
