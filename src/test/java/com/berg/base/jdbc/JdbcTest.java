package com.berg.base.jdbc;

import com.berg.utils.OutputUtil;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTest {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJdbc() {

        Connection connection = null;

        PreparedStatement preparedStatement = null;

        ResultSet resultSet = null;

        try{

            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mybatis", "root", "123456");

            String sql = "SELECT STUDENT_ID, DEPARTMENT_ID, NUMBER, NAME, BIRTHDAY FROM student WHERE STUDENT_ID = ?";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setLong(1, 1L);

            resultSet = preparedStatement.executeQuery();

            List<Student> studentList = new ArrayList<>();
            Student student = null;
            if(resultSet.next()){

                student = new Student();
                student.setStudentId(resultSet.getLong("STUDENT_ID"));
                student.setDepartmentId(resultSet.getLong("DEPARTMENT_ID"));
                student.setNumber(resultSet.getString("NUMBER"));
                student.setName(resultSet.getString("NAME"));
                student.setBirthday(resultSet.getDate("BIRTHDAY"));

                studentList.add(student);
            }

            OutputUtil.outputlist(studentList);

        }catch (SQLException e){

            e.printStackTrace();
        }finally {

            try{

                if(connection != null){

                    connection.close();
                }

                if(preparedStatement != null){

                    preparedStatement.close();
                }

                if(resultSet != null){

                    resultSet.close();
                }

            }catch (SQLException e){

                e.printStackTrace();
            }
        }
    }
}
