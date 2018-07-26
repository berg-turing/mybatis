package com.berg.application.secondclass.mapper;

import com.berg.application.secondclass.dto.DepartmentDto;
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

public class DepartmentMapperTest {

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
            DepartmentMapper mapper = sqlSession.getMapper(DepartmentMapper.class);

            DepartmentDto departmentDto = new DepartmentDto();
//            departmentDto.setDepartmentId(1L);
//            departmentDto.setName("计算机");
            List<DepartmentDto> select = mapper.select(departmentDto);

            OutputUtil.outputlist(select);
        }finally {
            sqlSession.close();
        }

    }
}
