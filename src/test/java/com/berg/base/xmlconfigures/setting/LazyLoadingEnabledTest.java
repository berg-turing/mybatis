package com.berg.base.xmlconfigures.setting;

import com.berg.base.support.dto.StudentDto;
import com.berg.base.support.mapper.StudentMapper;
import com.berg.utils.OutputUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * 测试lazyLoadingEnabled设置项
 *
 * 用于配置MyBatis的高级查询(association\collections)是否是延迟加载，
 * 如果该配置项为true，则高级查询会延迟加载(即在使用的时候才会加载关联的对象)
 *
 */
public class LazyLoadingEnabledTest {

    /**
     * 测试当lazyLoadingEnabled的值为true的时候
     */
    @Test
    public void testTrue() throws IOException {

        SqlSessionFactory sessionFactory =
                new SqlSessionFactoryBuilder().
                        build(Resources
                                .getResourceAsStream("config/base/xmlconfigures/setting/lazyloadingenabled/true.xml"));

        SqlSession sqlSession = sessionFactory.openSession();

        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

        StudentDto studentDto = new StudentDto();
        studentDto.setStudentId(1L);

        List<StudentDto> studentDtoList = mapper.selectWithExtends(studentDto);

        if(studentDtoList.size() > 0){

            StudentDto dto = studentDtoList.get(0);

            System.out.println("输出Student对象：");
            System.out.println(dto.getName());

            System.out.println("使用关联的数据：");

            System.out.println(dto.getDepartmentDto());

            System.out.println(dto.getHobbyDtos());
        }
    }

    /**
     * 测试当lazyLoadingEnabled的值为false的时候
     */
    @Test
    public void testFalse() throws IOException {

        SqlSessionFactory sessionFactory =
                new SqlSessionFactoryBuilder().
                        build(Resources
                                .getResourceAsStream("config/base/xmlconfigures/setting/lazyloadingenabled/false.xml"));

        SqlSession sqlSession = sessionFactory.openSession();

        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);

        StudentDto studentDto = new StudentDto();
        studentDto.setStudentId(1L);

        List<StudentDto> studentDtoList = mapper.selectWithExtends(studentDto);

        if(studentDtoList.size() > 0){

            StudentDto dto = studentDtoList.get(0);

            System.out.println("输出Student对象：");
            System.out.println(dto.getName());

            System.out.println("使用关联的数据：");

            System.out.println(dto.getDepartmentDto());

            System.out.println(dto.getHobbyDtos());
        }
    }
}
