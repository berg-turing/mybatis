package com.berg.firstclass.mapper;

import com.berg.firstclass.dto.StudentDto;
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
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelectOne(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

            StudentDto studentDto = mapper.selectOne(1L);

            System.out.println(studentDto);
        }finally {
            sqlSession.close();
        }

    }

    @Test
    public void testSelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

            StudentDto studentDto = new StudentDto();

            List<StudentDto> select = mapper.select(studentDto);

            OutputUtil.OutputList(select);
        }finally {
            sqlSession.close();
        }

    }

    @Test
    public void testSelectWithExtends(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

            StudentDto studentDto = new StudentDto();

            List<StudentDto> select = mapper.selectWithExtends(studentDto);

            OutputUtil.OutputList(select);
        }finally {
            sqlSession.close();
        }

    }

}
