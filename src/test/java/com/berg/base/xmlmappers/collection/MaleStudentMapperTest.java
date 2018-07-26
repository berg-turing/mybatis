package com.berg.base.xmlmappers.collection;

import com.berg.base.xmlmappers.collection.dto.Malestudent;
import com.berg.base.xmlmappers.collection.mapper.MaleStudentMapper;
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

public class MaleStudentMapperTest {

    private SqlSessionFactory sqlSessionFactory = null;

    @Before
    public void before() throws IOException {
        String resource = "config/base/xmlmappers/collection/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelectCollectionBySelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleStudentMapper mapper = sqlSession.getMapper(MaleStudentMapper.class);

        List<Malestudent> select = mapper.selectCollectionBySelect(null);

        OutputUtil.outputlist(select);

        sqlSession.close();
    }

    @Test
    public void testSelectCollectionByResultMap(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleStudentMapper mapper = sqlSession.getMapper(MaleStudentMapper.class);

        List<Malestudent> select = mapper.selectCollectionByResultMap(null);

        OutputUtil.outputlist(select);

        sqlSession.close();
    }
}
