package com.berg.base.support.mapper;

import com.berg.base.support.dto.StudentHobbyDto;

import java.util.List;

public interface StudentHobbyMapper {

    /**
     * 根据id查找学生爱好映射对象
     *
     * @param studentHobbyId    学生爱好id
     * @return                  查找到的学生爱好对象
     */
    StudentHobbyDto selectOne(Long studentHobbyId);

    /**
     * 条件查询学生爱好映射对象
     *
     * @param studentHobbyDto       查询条件
     * @return                      满足条件的学生爱好映射对象
     */
    List<StudentHobbyDto> select(StudentHobbyDto studentHobbyDto);

    /**
     * 带关联的查询学生爱好映射对象
     *
     * @param studentHobbyDto       查询条件
     * @return                      满足条件的带关联的学生爱好映射对象
     */
    List<StudentHobbyDto> selectWithExtends(StudentHobbyDto studentHobbyDto);
}
