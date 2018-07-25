package com.berg.base.xmlmappers.select;

import com.berg.base.support.dto.DepartmentDto;
import com.berg.base.support.mapper.DepartmentMapper;
import com.berg.utils.OutputUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;

/**
 * 动态sql的测试
 */
public class DynamicSqlTest {

    @Test
    public void testDynamicSql() throws IOException {

        SqlSessionFactory sqlSessionFactory =
                new SqlSessionFactoryBuilder()
                        .build(Resources
                                .getResourceAsStream("config/base/xmlmappers/select/dynamicsql.xml"));

        SqlSession sqlSession = sqlSessionFactory.openSession();

        DepartmentMapper mapper = sqlSession.getMapper(DepartmentMapper.class);

        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setDepartmentId(1L);

        OutputUtil.outputlist(mapper.select(departmentDto));

        OutputUtil.outputlist(mapper.select(departmentDto));

        sqlSession.close();
    }
}
