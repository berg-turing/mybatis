package com.berg.base.xmlconfigures.typehandler;

import com.berg.base.xmlconfigures.typealiases.Employee;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 员工类型转换器
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
public class EmployeeTypeHandler implements TypeHandler<Employee>{

    /**
     * 分隔符
     */
    private static String SPLIT_SYM = ",";

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Employee employee, JdbcType jdbcType) throws SQLException {

        preparedStatement.setString(i, String.valueOf(employee.getId() + SPLIT_SYM + employee.getName()));
    }

    @Override
    public Employee getResult(ResultSet resultSet, String s) throws SQLException {

        return processString(resultSet.getString(s));
    }

    @Override
    public Employee getResult(ResultSet resultSet, int i) throws SQLException {

        return processString(resultSet.getString(i));
    }

    @Override
    public Employee getResult(CallableStatement callableStatement, int i) throws SQLException {

        return processString(callableStatement.getString(i));
    }

    /**
     * 解析字符串
     * @param string    需要解析的字符串
     * @return          字符串的解析结果
     */
    private Employee processString(String string){
        Employee employee = new Employee();

        String[] split = string.split(SPLIT_SYM);

        if(split.length == 2){
            employee.setId(new Long(split[0]));
            employee.setName(split[1]);
        }

        return employee;
    }
}
