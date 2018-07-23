package com.berg.base.xmlconfigures.setting;

import com.berg.base.support.mapper.DepartmentMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;

/**
 * 测试localCacheScope设置项
 *
 * 本地缓存的范围
 */
public class LocalCacheScopeTest {

    /**
     * 测试localCacheScope的值为STATEMENT
     *
     * 使用了同一个Statement对象
     */
    @Test
    public void testStatement() throws IOException {

        SqlSessionFactory sessionFactory =
                new SqlSessionFactoryBuilder().
                        build(Resources
                                .getResourceAsStream("config/base/xmlconfigures/setting/localcachescope/statement.xml"));

        System.out.println("获取第一个session");
        SqlSession sqlSession1 = sessionFactory.openSession();

        System.out.println("获取第一个mapper");
        //获取mapper
        DepartmentMapper mapper11 = sqlSession1.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper11.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper11.selectOne(1L));

        System.out.println("获取第二个mapper");
        //获取mapper
        DepartmentMapper mapper12 = sqlSession1.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper12.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper12.selectOne(1L));



        System.out.println("获取第二个session");
        SqlSession sqlSession2 = sessionFactory.openSession();

        System.out.println("获取第一个mapper");
        //获取mapper
        DepartmentMapper mapper21 = sqlSession2.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper21.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper21.selectOne(1L));

        System.out.println("获取第二个mapper");
        //获取mapper
        DepartmentMapper mapper22 = sqlSession2.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper22.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper22.selectOne(1L));
    }

    /**
     * 测试localCacheScope的值为SESSION
     *
     * 共用同一个session
     */
    @Test
    public void testSession() throws IOException {

        SqlSessionFactory sessionFactory =
                new SqlSessionFactoryBuilder().
                        build(Resources
                                .getResourceAsStream("config/base/xmlconfigures/setting/localcachescope/session.xml"));

        System.out.println("获取第一个session");
        SqlSession sqlSession1 = sessionFactory.openSession();

        System.out.println("获取第一个mapper");
        //获取mapper
        DepartmentMapper mapper11 = sqlSession1.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper11.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper11.selectOne(1L));

        System.out.println("获取第二个mapper");
        //获取mapper
        DepartmentMapper mapper12 = sqlSession1.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper12.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper12.selectOne(1L));



        System.out.println("获取第二个session");
        SqlSession sqlSession2 = sessionFactory.openSession();

        System.out.println("获取第一个mapper");
        //获取mapper
        DepartmentMapper mapper21 = sqlSession2.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper21.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper21.selectOne(2L));

        System.out.println("获取第二个mapper");
        //获取mapper
        DepartmentMapper mapper22 = sqlSession2.getMapper(DepartmentMapper.class);
        System.out.println("第一次查询：");
        //查询一次
        System.out.println(mapper22.selectOne(1L));
        System.out.println("第二次查询：");
        //查询两次
        System.out.println(mapper22.selectOne(2L));
    }
}
