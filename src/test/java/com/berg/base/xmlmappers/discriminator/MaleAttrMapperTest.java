package com.berg.base.xmlmappers.discriminator;

import com.berg.base.xmlmappers.discriminator.dto.MaleAttr;
import com.berg.base.xmlmappers.discriminator.mapper.MaleAttrMapper;
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

public class MaleAttrMapperTest {

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

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        List<MaleAttr> maleAttrs = mapper.fineMore(1L);

        OutputUtil.outputlist(maleAttrs);

        sqlSession.close();
    }
}
