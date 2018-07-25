package com.berg.base.xmlmappers.associate;

import com.berg.base.xmlmappers.associate.dto.Department;
import com.berg.base.xmlmappers.associate.mapper.DepartmentMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class DepartmentMapperTest {

    private SqlSessionFactory sqlSessionFactory = null;

    @Before
    public void before() throws IOException {
        String resource = "config/base/xmlmappers/associate/mybatis.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelectOne(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        DepartmentMapper mapper = sqlSession.getMapper(DepartmentMapper.class);

        Department department = mapper.selectOne(1L);

        System.out.println(department);

        sqlSession.close();
    }
}
