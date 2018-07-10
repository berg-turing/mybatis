package com.berg.base.xmlconfigure.typehandler;

import com.berg.base.xmlconfigure.typealiases.Student;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 学生类型转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class StudentTypeHandler extends BaseTypeHandler<Student>{

    /**
     * 分隔符
     */
    private static String SPLIT_SYM = ",";

    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Student student, JdbcType jdbcType) throws SQLException {

        preparedStatement.setString(i, String.valueOf(student.getId()) + SPLIT_SYM + student.getName());
    }

    public Student getNullableResult(ResultSet resultSet, String s) throws SQLException {

        return processString(resultSet.getString(s));
    }

    public Student getNullableResult(ResultSet resultSet, int i) throws SQLException {

        return processString(resultSet.getString(i));
    }

    public Student getNullableResult(CallableStatement callableStatement, int i) throws SQLException {

        return processString(callableStatement.getString(i));
    }

    /**
     * 解析字符串
     * @param string    需要解析的字符串
     * @return          字符串的解析结果
     */
    private Student processString(String string){
        Student student = new Student();

        String[] split = string.split(SPLIT_SYM);

        if(split.length == 2){
            student.setId(new Long(split[0]));
            student.setName(split[1]);
        }

        return student;
    }
}
