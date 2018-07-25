package com.berg.base.xmlmappers.collection;

import com.berg.base.xmlmappers.collection.dto.MaleAttr;
import com.berg.base.xmlmappers.collection.mapper.MaleAttrMapper;
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
        String resource = "config/base/xmlmappers/collection/mybatis.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testFindMore(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        List<MaleAttr> maleAttrs = mapper.fineMore(1L);

        OutputUtil.outputlist(maleAttrs);
    }
}
