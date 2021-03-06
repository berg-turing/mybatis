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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * 可重用的执行器，该执行器会缓存Statement对象
 *
 * @author Clinton Begin
 */
public class ReuseExecutor extends BaseExecutor {

    /**
     * statement map对象
     * 用于缓存Statement对象的
     */
    private final Map<String, Statement> statementMap = new HashMap<String, Statement>();

    /**
     * @param configuration     配置对象
     * @param transaction       事务对象
     */
    public ReuseExecutor(Configuration configuration,
                         Transaction transaction) {

        super(configuration, transaction);
    }

    @Override
    public int doUpdate(MappedStatement ms,
                        Object parameter) throws SQLException {

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

        //预处理，并设置参数
        Statement stmt = prepareStatement(handler, ms.getStatementLog());

        //返回处理结果
        return handler.update(stmt);
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms,
                               Object parameter,
                               RowBounds rowBounds,
                               ResultHandler resultHandler,
                               BoundSql boundSql) throws SQLException {

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

        //预处理，并设置参数
        Statement stmt = prepareStatement(handler, ms.getStatementLog());

        //返回查询结果
        return handler.<E>query(stmt, resultHandler);
    }

    @Override
    protected <E> Cursor<E> doQueryCursor(MappedStatement ms,
                                          Object parameter,
                                          RowBounds rowBounds,
                                          BoundSql boundSql) throws SQLException {

        //获取配置对象
        Configuration configuration = ms.getConfiguration();

        //创建SatementHandler对象
        StatementHandler handler =
                configuration.
                        newStatementHandler(
                                wrapper,
                                ms,
                                parameter,
                                rowBounds,
                                null,
                                boundSql);

        //预处理，并设置参数
        Statement stmt = prepareStatement(handler, ms.getStatementLog());

        //返回查询结果
        return handler.<E>queryCursor(stmt);
    }

    @Override
    public List<BatchResult> doFlushStatements(boolean isRollback) throws SQLException {

        //循环关闭Statement对象
        for (Statement stmt : statementMap.values()) {

            //关闭Statement对象
            closeStatement(stmt);
        }

        //清空Statement的Map对象
        statementMap.clear();

        //返回空值
        return Collections.emptyList();
    }

    /**
     * 创建并设置相关参数成功的Statement对象
     *
     * @param handler      Statement处理器
     * @param statementLog 日志打印对象
     * @return 创建并设置相关参数成功的Statement对象
     * @throws SQLException sql异常
     */
    private Statement prepareStatement(StatementHandler handler, Log statementLog) throws SQLException {

        //Statement对象
        Statement stmt;

        //获取sql
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();

        //查看Statement的Map是否有已有的Statement
        //缓存的Statement以sql语句为key保存
        if (hasStatementFor(sql)) {
            //如果有缓存的Statement，就直接使用该Statement
            //当localCacheScope为STATEMENT时，就是通过这里来完成缓存的

            stmt = getStatement(sql);
            applyTransactionTimeout(stmt);
        } else {
            //没有找到相应的Statement，就创建Statement

            Connection connection = getConnection(statementLog);

            //创建Statement对象，并设置相关的参数
            stmt = handler.prepare(connection, transaction.getTimeout());

            //缓存Statement对象
            putStatement(sql, stmt);
        }

        //处理参数
        handler.parameterize(stmt);

        //返回Statement对象
        return stmt;
    }

    /**
     * @param sql
     * @return
     */
    private boolean hasStatementFor(String sql) {
        try {
            return statementMap.keySet().contains(sql) && !statementMap.get(sql).getConnection().isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private Statement getStatement(String s) {

        return statementMap.get(s);
    }

    private void putStatement(String sql, Statement stmt) {
        statementMap.put(sql, stmt);
    }

}
