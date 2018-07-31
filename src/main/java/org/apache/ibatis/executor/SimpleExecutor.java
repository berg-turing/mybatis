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
package org.apache.ibatis.executor;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * 简单执行器
 *
 * @author Clinton Begin
 */
public class SimpleExecutor extends BaseExecutor {

    /**
     * 简单执行器的构造函数
     *
     * @param configuration 配置对象
     * @param transaction   事务对象
     */
    public SimpleExecutor(Configuration configuration,
                          Transaction transaction) {

        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms,
                        Object parameter) throws SQLException {

        //Statement对象
        Statement stmt = null;

        try {

            //获取配置对象
            Configuration configuration = ms.getConfiguration();

            //创建StatementHandler对象
            StatementHandler handler =
                    configuration.
                            newStatementHandler(
                                    this,
                                    ms,
                                    parameter,
                                    RowBounds.DEFAULT,
                                    null,
                                    null);

            //执行预处理和参数处理
            stmt = prepareStatement(handler, ms.getStatementLog());

            //执行更新，并返回更新结果
            return handler.update(stmt);
        } finally {

            //关闭Statement对象
            closeStatement(stmt);
        }
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms,
                               Object parameter,
                               RowBounds rowBounds,
                               ResultHandler resultHandler,
                               BoundSql boundSql) throws SQLException {

        //Statement对象
        Statement stmt = null;

        try {

            //获取配置对象
            Configuration configuration = ms.getConfiguration();

            //创建StatementHandler
            StatementHandler handler =
                    configuration.
                            newStatementHandler(
                                    wrapper,
                                    ms,
                                    parameter,
                                    rowBounds,
                                    resultHandler,
                                    boundSql);

            //执行预处理和参数处理
            stmt = prepareStatement(handler, ms.getStatementLog());

            //执行查询，并返回查询结果
            return handler.<E>query(stmt, resultHandler);
        } finally {

            //关闭Statement对象
            closeStatement(stmt);
        }
    }

    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms,
                                          Object parameter,
                                          RowBounds rowBounds,
                                          BoundSql boundSql) throws SQLException {

        //获取配置对象
        Configuration configuration = ms.getConfiguration();

        //创建StatementHandler对象
        StatementHandler handler =
                configuration.
                        newStatementHandler(
                                wrapper,
                                ms,
                                parameter,
                                rowBounds,
                                null,
                                boundSql);

        //执行预处理和参数处理
        Statement stmt = prepareStatement(handler, ms.getStatementLog());

        //执行查询Cursor，并返回结果
        return handler.<E>queryCursor(stmt);
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {
        return Collections.emptyList();
    }

    /**
     * 预处理Statement和设置参数
     *
     * @param handler       StatementHandler
     * @param statementLog  statementLog
     * @return              Statement对象
     * @throws SQLException Sql语句异常
     */
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {

        //Statement对象
        Statement stmt;

        //获取链接对象
        Connection connection = getConnection(statementLog);

        //预处理
        stmt = handler.prepare(connection, transaction.getTimeout());

        //处理参数
        handler.parameterize(stmt);

        //返回Statement对象
        return stmt;
    }

}
