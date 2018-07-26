package com.berg.base.xmlmappers.delete;

import com.berg.base.xmlmappers.delete.dto.MaleAttr;
import com.berg.base.xmlmappers.delete.mapper.MaleAttrMapper;
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
        String resource = "config/base/xmlmappers/delete/mybatis-config.xml";
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

    @Test
    public void testDeleteById(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        System.out.println(mapper.deleteById(1L));

        OutputUtil.outputlist(mapper.select(null));

        //关闭提交，防止真正的删除了数据
        //sqlSession.commit();

        sqlSession.close();
    }

    @Test
    public void testDeleteByCondition(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        MaleAttr maleAttr = new MaleAttr();
        maleAttr.setId(1L);

        System.out.println(mapper.deleteByCondition(maleAttr));

        System.out.println(mapper.deleteByCondition(null));

        OutputUtil.outputlist(mapper.select(null));

        //关闭提交，防止真正的删除了数据
        //sqlSession.commit();

        sqlSession.close();
    }

}
