/**
 * Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.session.defaults;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

/**
 * @author Clinton Begin
 */

/**
 * 默认的SqlSessionFactory实现
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    /**
     * 配置对象
     */
    private final Configuration configuration;


    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
    }

    @Override
    public SqlSession openSession(ExecutorType execType) {
        return openSessionFromDataSource(execType, null, false);
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return openSessionFromDataSource(execType, level, false);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return openSessionFromDataSource(execType, null, autoCommit);
    }

    @Override
    public SqlSession openSession(Connection connection) {
        return openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection) {
        return openSessionFromConnection(execType, connection);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 从数据源里打开一个SqlSession
     *
     * @param execType   执行器的类型
     *                   SIMPLE
     *                   REUSE
     *                   BATCH
     * @param level      事务等级
     *                   NONE(Connection.TRANSACTION_NONE),
     *                   READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
     *                   READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
     *                   REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
     *                   SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
     * @param autoCommit 是否自动提交
     * @return 创建的SqlSession
     */
    private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {

        //事务对象
        Transaction tx = null;

        try {

            //环境对象
            final Environment environment = configuration.getEnvironment();

            //事务工厂
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);

            //事务对象
            tx = transactionFactory.newTransaction(environment.getDataSource(), level, autoCommit);

            //执行器
            final Executor executor = configuration.newExecutor(tx, execType);

            //返回SqlSession对象
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {

            // may have fetched a connection so lets call close()
            closeTransaction(tx);
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {

            ErrorContext.instance().reset();
        }
    }

    private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
        try {
            boolean autoCommit;
            try {
                autoCommit = connection.getAutoCommit();
            } catch (SQLException e) {
                // Failover to true, as most poor drivers
                // or databases won't support transactions
                autoCommit = true;
            }
            final Environment environment = configuration.getEnvironment();
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            final Transaction tx = transactionFactory.newTransaction(connection);
            final Executor executor = configuration.newExecutor(tx, execType);
            return new DefaultSqlSession(configuration, executor, autoCommit);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * 通过环境对象获取事务工厂
     *
     * @param environment       环境对象
     * @return                  事务工厂
     */
    private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {

        if (environment == null || environment.getTransactionFactory() == null) {

            return new ManagedTransactionFactory();
        }

        return environment.getTransactionFactory();
    }

    /**
     * 关闭事务
     *
     * @param tx    事务对象
     */
    private void closeTransaction(Transaction tx) {
        if (tx != null) {
            try {
                tx.close();
            } catch (SQLException ignore) {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }

}
