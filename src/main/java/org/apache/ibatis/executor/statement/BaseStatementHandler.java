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

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * 基本的StatementHandler
 * 封装了StatementHandler通用的操作和属性
 *
 * @author Clinton Begin
 */
public abstract class BaseStatementHandler implements StatementHandler {

    /**
     * 配置对象
     */
    protected final Configuration configuration;

    /**
     * 对象工厂
     */
    protected final ObjectFactory objectFactory;

    /**
     * 类型处理器注册对象
     */
    protected final TypeHandlerRegistry typeHandlerRegistry;

    /**
     * 结果处理器
     */
    protected final ResultSetHandler resultSetHandler;

    /**
     * 参数处理器
     */
    protected final ParameterHandler parameterHandler;

    /**
     * 执行器
     */
    protected final Executor executor;

    /**
     * 该StatementHandler处理的MappedStatement对象
     */
    protected final MappedStatement mappedStatement;

    /**
     * 分页对象
     */
    protected final RowBounds rowBounds;

    /**
     * Sql语句对象
     */
    protected BoundSql boundSql;

    /**
     * BaseStatementHandler的构造函数
     *
     * @param executor          执行器
     * @param mappedStatement   MappedStatement对象
     * @param parameterObject   参数对象
     * @param rowBounds         分页对象
     * @param resultHandler     结果处理器
     * @param boundSql          Sql语句对象
     */
    protected BaseStatementHandler(Executor executor,
                                   MappedStatement mappedStatement,
                                   Object parameterObject,
                                   RowBounds rowBounds,
                                   ResultHandler resultHandler,
                                   BoundSql boundSql) {

        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;

        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        this.objectFactory = configuration.getObjectFactory();

        // issue #435, get the key before calculating the statement
        if (boundSql == null) {
            //
            generateKeys(parameterObject);
            //
            boundSql = mappedStatement.getBoundSql(parameterObject);
        }

        this.boundSql = boundSql;

        //创建参数处理器
        this.parameterHandler =
                configuration.
                        newParameterHandler(
                                mappedStatement,
                                parameterObject,
                                boundSql);

        //创建结果集处理器
        this.resultSetHandler =
                configuration.
                        newResultSetHandler(
                                executor,
                                mappedStatement,
                                rowBounds,
                                parameterHandler,
                                resultHandler,
                                boundSql);
    }

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return parameterHandler;
    }

    @Override
    public Statement prepare(Connection connection, Integer transactionTimeout) throws SQLException {

        ErrorContext.instance().sql(boundSql.getSql());

        //Statement对象
        Statement statement = null;

        try {

            //初始化预处理对象
            statement = instantiateStatement(connection);

            //设置事务超时间
            setStatementTimeout(statement, transactionTimeout);

            //获取记录总条数的设置
            setFetchSize(statement);

            //返回Statement对象
            return statement;
        } catch (SQLException e) {

            closeStatement(statement);
            throw e;
        } catch (Exception e) {

            closeStatement(statement);
            throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    /**
     * 初始化Statement对象
     * 如果是普通的Statement就直接生成
     * 如果是预处理Statement就是预处理Sql并生成预处理Statement对象
     *
     * @param connection        数据库连接对象
     * @return                  Statement对象
     * @throws SQLException     Sql语句异常
     */
    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    /**
     * 设置超时时间
     *
     * @param stmt                  Statement对象
     * @param transactionTimeout    超时时间
     * @throws SQLException         Sql语句异常
     */
    protected void setStatementTimeout(Statement stmt,
                                       Integer transactionTimeout) throws SQLException {

        Integer queryTimeout = null;

        if (mappedStatement.getTimeout() != null) {

            queryTimeout = mappedStatement.getTimeout();
        } else if (configuration.getDefaultStatementTimeout() != null) {

            queryTimeout = configuration.getDefaultStatementTimeout();
        }

        if (queryTimeout != null) {

            stmt.setQueryTimeout(queryTimeout);
        }

        StatementUtil.applyTransactionTimeout(stmt, queryTimeout, transactionTimeout);
    }

    /**
     * 获取记录总条数的设定
     *
     * @param stmt              Statement对象
     * @throws SQLException     Sql语句异常
     */
    protected void setFetchSize(Statement stmt) throws SQLException {

        //获取fetchSize
        Integer fetchSize = mappedStatement.getFetchSize();

        //如果值不为空，就设置当前值
        if (fetchSize != null) {

            stmt.setFetchSize(fetchSize);
            return;
        }

        //获取系统配置的默认值
        Integer defaultFetchSize = configuration.getDefaultFetchSize();

        //如果系统配置的默认值不为空，就设置系统的默认值
        if (defaultFetchSize != null) {

            stmt.setFetchSize(defaultFetchSize);
        }

        //其他情况就是数据库驱动商设置的JDBC的默认设置了
    }

    /**
     * 关闭Statement对象
     *
     * @param statement     Statement对象
     */
    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            //ignore
        }
    }

    /**
     *
     * @param parameter
     */
    protected void generateKeys(Object parameter) {

        KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();

        ErrorContext.instance().store();

        keyGenerator.processBefore(executor, mappedStatement, null, parameter);

        ErrorContext.instance().recall();
    }

}
