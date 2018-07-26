package com.berg.base.xmlmappers.discriminator;

import com.berg.base.xmlmappers.discriminator.dto.BaseStudent;
import com.berg.base.xmlmappers.discriminator.mapper.StudentMapper;
import com.berg.utils.OutputUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class StudentMapperTest {

    private SqlSessionFactory sqlSessionFactory = null;

    @Before
    public void before() throws IOException {
        String resource = "config/base/xmlmappers/discriminator/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelectDiscriminatorBySelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

        List<BaseStudent> select = mapper.selectDiscriminatorBySelect(null);

        OutputUtil.outputlist(select);

        sqlSession.close();
    }

    @Test
    public void testSelectDiscriminatorByResultMap(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

        List<BaseStudent> select = mapper.selectDiscriminatorByResultMap(null);

        OutputUtil.outputlist(select);

        sqlSession.close();
    }
}
