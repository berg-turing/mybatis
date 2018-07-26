package com.berg.base.xmlmappers.associate;

import com.berg.base.xmlmappers.associate.dto.Student;
import com.berg.base.xmlmappers.associate.mapper.StudentMapper;
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
        String resource = "config/base/xmlmappers/associate/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelectAssociateBySelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

        List<Student> select = mapper.selectAssociateBySelect(null);

        OutputUtil.outputlist(select);

        sqlSession.close();
    }

    @Test
    public void testSelectAssociateByResultMap(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

        List<Student> select = mapper.selectAssociateByResultMap(null);

        OutputUtil.outputlist(select);

        sqlSession.close();
    }
}
