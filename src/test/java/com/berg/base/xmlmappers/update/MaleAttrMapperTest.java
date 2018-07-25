package com.berg.base.xmlmappers.update;

import com.berg.base.xmlmappers.update.dto.MaleAttr;
import com.berg.base.xmlmappers.update.mapper.MaleAttrMapper;
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
        String resource = "config/base/xmlmappers/update/mybatis.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);

        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Test
    public void testSelect(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        List<MaleAttr> maleAttrList = mapper.select(null);

        OutputUtil.outputlist(maleAttrList);

        sqlSession.close();
    }


}
