package com.berg.base.sqlsessionfactory;

import com.berg.application.firstclass.dto.DepartmentDto;
import com.berg.application.firstclass.mapper.DepartmentMapper;
import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

public class BuildSqlSessionFactoryTest {

    /**
     * 通过xml文件来构建一个SqlSessionFactory
     */
    @Test
    public void buildByXmlFile(){

        try{
            String path = "mybatis-config.xml";

            InputStream resource = Resources.getResourceAsStream(path);

            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resource);


            SqlSession sqlSession = sqlSessionFactory.openSession();

            try{
                DepartmentMapper mapper = sqlSession.getMapper(DepartmentMapper.class);

                DepartmentDto departmentDto = mapper.selectOne(1L);

                System.out.println(departmentDto);
            }finally {
                sqlSession.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 通过java代码来构建一个SqlSessionFactory
     */
    @Test
    public void buildByJavaCode(){

        Properties properties = new Properties();
        properties.setProperty("driver", "com.mysql.jdbc.Driver");
        properties.setProperty("url", "jdbc:mysql://localhost:3306/mybatis");
        properties.setProperty("username", "root");
        properties.setProperty("password", "123456");

        PooledDataSourceFactory dataSourceFactory = new PooledDataSourceFactory();
        dataSourceFactory.setProperties(properties);
        DataSource dataSource = dataSourceFactory.getDataSource();

        JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();

        Environment environment = new Environment("develope", transactionFactory, dataSource);

        Configuration configuration = new Configuration(environment);
        configuration.addMapper(DepartmentMapper.class);

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

        SqlSession sqlSession = sqlSessionFactory.openSession();

        try{
            DepartmentMapper mapper = sqlSession.getMapper(DepartmentMapper.class);

            DepartmentDto departmentDto = mapper.selectOne(1L);

            System.out.println(departmentDto);
        }finally {
            sqlSession.close();
        }
    }
}
