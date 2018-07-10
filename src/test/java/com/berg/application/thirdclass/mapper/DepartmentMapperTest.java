package com.berg.application.thirdclass.mapper;

import com.berg.application.thirdclass.dto.DepartmentDto;
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
        String resource = "mybatis-config.xml";
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

            OutputUtil.OutputList(select);
        }finally {
            sqlSession.close();
        }

    }
}
