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
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MaleAttrMapperTest {

    private SqlSessionFactory sqlSessionFactory = null;

    private Random random = new Random(new Date().getTime());

    private static final Integer MAX_STUDENT_ID_ = 19;

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

    @Test
    public void testUpdateById(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        MaleAttr maleAttr = new MaleAttr();

        maleAttr.setId(9L);
        maleAttr.setStudentId(nextStudentId());
        maleAttr.setGame("king");

        System.out.println(mapper.updateById(maleAttr));

        System.out.println(maleAttr);

        OutputUtil.outputlist(mapper.select(maleAttr));

        sqlSession.commit();

        sqlSession.close();
    }

    @Test
    public void testUpdateByCondition(){

        SqlSession sqlSession = sqlSessionFactory.openSession();

        MaleAttrMapper mapper = sqlSession.getMapper(MaleAttrMapper.class);

        MaleAttr maleAttr = new MaleAttr();

        maleAttr.setId(9L);
        maleAttr.setStudentId(nextStudentId());
        maleAttr.setGame("king");

        MaleAttr condition = new MaleAttr();
        condition.setStudentId(nextStudentId());

        System.out.println(mapper.updateByCondition(maleAttr, condition));

        System.out.println(maleAttr);

        OutputUtil.outputlist(mapper.select(maleAttr));

        sqlSession.commit();

        sqlSession.close();
    }


    private Long nextStudentId(){

        return random.nextInt(MAX_STUDENT_ID_) + 1L;
    }


}
