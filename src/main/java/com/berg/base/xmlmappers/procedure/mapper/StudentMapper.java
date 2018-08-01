package com.berg.base.xmlmappers.procedure.mapper;

import com.berg.base.xmlmappers.procedure.dto.PageStudent;
import com.berg.base.xmlmappers.procedure.dto.StudentProcedure;

public interface StudentMapper {

    /**
     * 调用计数的存储过程
     *
     * @param studentProcedure  参数对象
     */
    void count(StudentProcedure studentProcedure);

    /**
     *
     * @param pageStudent
     */
    void findStudent(PageStudent pageStudent);
}
