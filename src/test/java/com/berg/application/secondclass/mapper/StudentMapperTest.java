package com.berg.application.secondclass.mapper;

import com.berg.application.secondclass.dto.DepartmentDto;
import com.berg.application.secondclass.dto.StudentDto;
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
        String resource = "config/application/secondclass/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

            StudentDto studentDto = new StudentDto();

            List<StudentDto> select = mapper.select(studentDto);

            OutputUtil.outputlist(select);
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

            DepartmentDto departmentDto = new DepartmentDto();
            departmentDto.setDepartmentId(6L);

            studentDto.setDepartmentDto(departmentDto);

            List<StudentDto> select = mapper.selectWithExtends(studentDto);

            OutputUtil.outputlist(select);
        }finally {
            sqlSession.close();
        }

    }

}
