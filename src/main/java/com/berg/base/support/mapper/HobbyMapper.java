package com.berg.base.support.mapper;

import com.berg.base.support.dto.HobbyDto;

import java.util.List;

/**
 * 爱好的mapper
 */
public interface HobbyMapper {

    /**
     * 根据id，查找一个爱好对象
     *
     * @param hobbyId   爱好对象的id
     * @return          查找到的爱好对象
     */
    HobbyDto selectOne(Long hobbyId);

    /**
     * 根据学生id，查找多个爱好对象
     *
     * @param studentId     学生id
     * @return              满足条件的爱好对象
     */
    List<HobbyDto> selectMore(Long studentId);

    /**
     * 条件查询爱好对象
     *
     * @param hobbyDto      查询条件
     * @return              满足条件的爱好对象
     */
    List<HobbyDto> select(HobbyDto hobbyDto);
}
