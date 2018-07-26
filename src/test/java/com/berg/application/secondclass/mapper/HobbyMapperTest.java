package com.berg.application.secondclass.mapper;

import com.berg.application.secondclass.dto.HobbyDto;
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

public class HobbyMapperTest {

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
            HobbyMapper mapper = sqlSession.getMapper(HobbyMapper.class);

            HobbyDto hobbyDto = new HobbyDto();
//            departmentDto.setHobbyId(1L);
//            hobbyDto.setName("打");
            List<HobbyDto> select = mapper.select(hobbyDto);

            OutputUtil.outputlist(select);
        }finally {
            sqlSession.close();
        }

    }
}
