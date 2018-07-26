package com.berg.base.xmlmappers.discriminator;

import com.berg.base.xmlmappers.discriminator.dto.FemaleAttr;
import com.berg.base.xmlmappers.discriminator.mapper.FemaleAttrMapper;
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

public class FemaleAttrMapperTest {

    private SqlSessionFactory sqlSessionFactory = null;

    @Before
    public void before() throws IOException {
        String resource = "config/base/xmlmappers/discriminator/mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testFindMore(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        FemaleAttrMapper mapper = sqlSession.getMapper(FemaleAttrMapper.class);

        List<FemaleAttr> femaleAttrs = mapper.fineMore(2L);

        OutputUtil.outputlist(femaleAttrs);

        sqlSession.close();
    }
}
