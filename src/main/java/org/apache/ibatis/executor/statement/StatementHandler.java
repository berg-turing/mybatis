/**
 * Copyright 2009-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.executor.statement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.ResultHandler;

/**
 * @author Clinton Begin
 */
public interface StatementHandler {

    /**
     * 预处理
     *
     * @param connection            数据库连接对象
     * @param transactionTimeout    事务超时时间
     * @return                      预处理之后的Statement对象
     * @throws SQLException         SQL语句执行异常
     */
    Statement prepare(Connection connection, Integer transactionTimeout)
            throws SQLException;

    /**
     * 参数化过程，在预处理对象之后处理参数的设置
     *
     * @param statement     Statement对象
     * @throws SQLException SQL语句执行异常
     */
    void parameterize(Statement statement)
            throws SQLException;

    /**
     *
     * @param statement
     * @throws SQLException
     */
    void batch(Statement statement)
            throws SQLException;

    /**
     * 更新操作
     *
     * @param statement     Statement对象
     * @return              更新处理结果影响的数据的条数
     * @throws SQLException SQL语句执行异常
     */
    int update(Statement statement)
            throws SQLException;

    /**
     * 普通查询操作
     *
     * @param statement         Statement对象
     * @param resultHandler     结果处理器对象
     * @param <E>               查询结果对象的泛型
     * @return                  查询结果
     * @throws SQLException     SQL语句执行异常
     */
    <E> List<E> query(Statement statement, ResultHandler resultHandler)
            throws SQLException;

    /**
     * Cursor查询操作
     *
     * @param statement     Statement对象
     * @param <E>           查询结果对象的泛型
     * @return              查询的Cursor对象
     * @throws SQLException SQL语句执行异常
     */
    <E> Cursor<E> queryCursor(Statement statement)
            throws SQLException;

    /**
     * 获取BoundSql对象
     *
     * @return  BoundSql对象
     */
    BoundSql getBoundSql();

    /**
     *  获取参数处理器
     *
     * @return  参数处理器
     */
    ParameterHandler getParameterHandler();

}
