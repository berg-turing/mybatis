package com.berg.secondclass.mapper;

import com.berg.secondclass.dto.StudentHobbyDto;
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

public class StudentHobbyMapperTest {

    private SqlSessionFactory sqlSessionFactory = null;

    @Before
    public void before() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            StudentHobbyMapper mapper = sqlSession.getMapper(StudentHobbyMapper.class);

            StudentHobbyDto studentHobbyDto = new StudentHobbyDto();
//            studentHobbyDto.setId(1L);
//            studentHobbyDto.setStudentId(1L);
//            studentHobbyDto.setHobbyId(1L);
            List<StudentHobbyDto> select = mapper.select(studentHobbyDto);

            OutputUtil.OutputList(select);
        }finally {
            sqlSession.close();
        }

    }

    @Test
    public void testSelectWithExtends(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            StudentHobbyMapper mapper = sqlSession.getMapper(StudentHobbyMapper.class);

            StudentHobbyDto studentHobbyDto = new StudentHobbyDto();
//            studentHobbyDto.setId(1L);
//            studentHobbyDto.setStudentId(1L);
//            studentHobbyDto.setHobbyId(1L);
            List<StudentHobbyDto> select = mapper.selectWithExtends(studentHobbyDto);

            OutputUtil.OutputList(select);
        }finally {
            sqlSession.close();
        }

    }
}
