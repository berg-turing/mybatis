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

import static org.apache.ibatis.executor.ExecutionPlaceholder.EXECUTION_PLACEHOLDER;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.statement.StatementUtil;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * 基本执行器，封装了执行器的通用操作
 *
 * @author Clinton Begin
 */
public abstract class BaseExecutor implements Executor {

    /**
     * 日志打印对象
     */
    private static final Log log = LogFactory.getLog(BaseExecutor.class);

    /**
     * 事务对象
     */
    protected Transaction transaction;

    /**
     * 该执行器的包装对象
     */
    protected Executor wrapper;

    /**
     *
     */
    protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;

    /**
     * 本地缓存
     */
    protected PerpetualCache localCache;

    /**
     *
     */
    protected PerpetualCache localOutputParameterCache;

    /**
     * 配置对象
     */
    protected Configuration configuration;

    /**
     *
     */
    protected int queryStack;

    /**
     * 是否已经关闭
     */
    private boolean closed;

    /**
     * 构造函数
     *
     * @param configuration 配置对象
     * @param transaction   事务对象
     */
    protected BaseExecutor(Configuration configuration, Transaction transaction) {

        //设置事务对象
        this.transaction = transaction;

        //
        this.deferredLoads = new ConcurrentLinkedQueue<DeferredLoad>();

        //创建本地缓存对象
        this.localCache = new PerpetualCache("LocalCache");

        //
        this.localOutputParameterCache = new PerpetualCache("LocalOutputParameterCache");

        //执行器是否关闭为false
        this.closed = false;

        //配置对象
        this.configuration = configuration;

        //默认包装对象为自己
        this.wrapper = this;
    }

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
        return transaction;
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {

                //回滚
                rollback(forceRollback);
            } finally {

                //关闭事务
                if (transaction != null) {
                    transaction.close();
                }
            }
        } catch (SQLException e) {
            // Ignore.  There's nothing that can be done at this point.
            log.warn("Unexpected exception on closing transaction.  Cause: " + e);
        } finally {

            //其他对象设置为空，释放内存空间
            transaction = null;
            deferredLoads = null;
            localCache = null;
            localOutputParameterCache = null;

            //标识已经关闭
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {

        //
        ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());

        //已经关闭则不执行
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }

        //清理本地缓存
        clearLocalCache();

        //具体的更新的操作，交给子类完成
        return doUpdate(ms, parameter);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return flushStatements(false);
    }

    /**
     * @param isRollBack
     * @return
     * @throws SQLException
     */
    public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }

        //
        return doFlushStatements(isRollBack);
    }

    @Override
    public <E> List<E> query(MappedStatement ms,
                             Object parameter,
                             RowBounds rowBounds,
                             ResultHandler resultHandler) throws SQLException {

        //获取sql
        BoundSql boundSql = ms.getBoundSql(parameter);

        //获取缓存key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);

        //查询
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> query(MappedStatement ms,
                             Object parameter,
                             RowBounds rowBounds,
                             ResultHandler resultHandler,
                             CacheKey key,
                             BoundSql boundSql) throws SQLException {

        //
        ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());

        //如果执行器已经关闭，抛出异常
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }

        //如果查询栈为空，同时该语句标识需要刷新缓存
        //就清理本地缓存
        if (queryStack == 0 && ms.isFlushCacheRequired()) {

            //清理本地缓存
            clearLocalCache();
        }

        //查询的结果
        List<E> list;

        try {

            //查询栈增加
            queryStack++;

            //获取缓存的值
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;


            if (list != null) {

                handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
            } else {

                //从数据库中查询数据
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {

            //查询栈减少
            queryStack--;
        }

        if (queryStack == 0) {

            for (DeferredLoad deferredLoad : deferredLoads) {
                deferredLoad.load();
            }

            // issue #601
            deferredLoads.clear();

            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                // issue #482
                clearLocalCache();
            }
        }

        //返回结果
        return list;
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {

        BoundSql boundSql = ms.getBoundSql(parameter);

        return doQueryCursor(ms, parameter, rowBounds, boundSql);
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {

        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }

        DeferredLoad deferredLoad = new DeferredLoad(resultObject, property, key, localCache, configuration, targetType);

        if (deferredLoad.canLoad()) {

            deferredLoad.load();
        } else {

            deferredLoads.add(new DeferredLoad(resultObject, property, key, localCache, configuration, targetType));
        }
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {

        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }

        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());

        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();

        // mimic DefaultParameterHandler logic
        for (ParameterMapping parameterMapping : parameterMappings) {

            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }

                cacheKey.update(value);
            }
        }

        if (configuration.getEnvironment() != null) {
            // issue #176
            cacheKey.update(configuration.getEnvironment().getId());
        }

        return cacheKey;
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return localCache.getObject(key) != null;
    }

    @Override
    public void commit(boolean required) throws SQLException {

        if (closed) {
            throw new ExecutorException("Cannot commit, transaction is already closed");
        }

        clearLocalCache();
        flushStatements();

        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {

        if (!closed) {

            try {

                clearLocalCache();
                flushStatements(true);
            } finally {

                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void clearLocalCache() {

        if (!closed) {

            localCache.clear();
            localOutputParameterCache.clear();
        }
    }




    //最终最为基础的三个操作
    //其他所有的操作(除了特殊的操作)都是基于这三个操作的

    /**
     * 更新操作
     *
     * @param ms        MappedStatement对象
     * @param parameter 处理之后的参数对象
     * @return 更新影响的行数
     * @throws SQLException SQL语句执行异常
     */
    protected abstract int doUpdate(MappedStatement ms,
                                    Object parameter)
            throws SQLException;

    /**
     * 查询操作
     *
     * @param ms            MappedStatement对象
     * @param parameter     处理之后的参数对象
     * @param rowBounds     分页对象
     * @param resultHandler 结果处理器
     * @param boundSql      Sql语句对象
     * @param <E>           查询结果的泛型
     * @return 查询的结果
     * @throws SQLException SQL语句执行异常
     */
    protected abstract <E> List<E> doQuery(MappedStatement ms,
                                           Object parameter,
                                           RowBounds rowBounds,
                                           ResultHandler resultHandler,
                                           BoundSql boundSql)
            throws SQLException;

    /**
     * 游标查询
     *
     * @param ms        MappedStatement对象
     * @param parameter 处理之后的参数对象
     * @param rowBounds 分页对象
     * @param boundSql  Sql语句对象
     * @param <E>       查询结果的泛型
     * @return 查询的结果
     * @throws SQLException SQL语句执行异常
     */
    protected abstract <E> Cursor<E> doQueryCursor(MappedStatement ms,
                                                   Object parameter,
                                                   RowBounds rowBounds,
                                                   BoundSql boundSql)
            throws SQLException;


    /**
     * 刷新Statement对象
     *
     * @param isRollback    是否回滚
     * @return              批量处理的结果，只有批处理的执行器才会返回数据
     * @throws SQLException Sql语句异常
     */
    protected abstract List<BatchResult> doFlushStatements(boolean isRollback)
            throws SQLException;

    /**
     * 关闭Statement对象
     *
     * @param statement 需要关闭的Statement对象
     */
    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Apply a transaction timeout.
     *
     * @param statement a current statement
     * @throws SQLException if a database access error occurs, this method is called on a closed <code>Statement</code>
     * @see StatementUtil#applyTransactionTimeout(Statement, Integer, Integer)
     * @since 3.4.0
     */
    protected void applyTransactionTimeout(Statement statement) throws SQLException {
        StatementUtil.applyTransactionTimeout(statement, statement.getQueryTimeout(), transaction.getTimeout());
    }

    /**
     * @param ms
     * @param key
     * @param parameter
     * @param boundSql
     */
    private void handleLocallyCachedOutputParameters(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {

        if (ms.getStatementType() == StatementType.CALLABLE) {

            final Object cachedParameter = localOutputParameterCache.getObject(key);

            if (cachedParameter != null && parameter != null) {

                final MetaObject metaCachedParameter = configuration.newMetaObject(cachedParameter);
                final MetaObject metaParameter = configuration.newMetaObject(parameter);

                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {

                    if (parameterMapping.getMode() != ParameterMode.IN) {

                        final String parameterName = parameterMapping.getProperty();
                        final Object cachedValue = metaCachedParameter.getValue(parameterName);
                        metaParameter.setValue(parameterName, cachedValue);
                    }
                }
            }
        }
    }

    /**
     * 从数据库中查找数据
     *
     * @param ms            语句对象
     * @param parameter     参数对象
     * @param rowBounds     分页对象
     * @param resultHandler 结果处理器对象
     * @param key           本地缓存的key
     * @param boundSql      获取sql语句的对象
     * @param <E>           查询时的结果参数类型
     * @return 查询的结果
     * @throws SQLException sql错误
     */
    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
        List<E> list;

        //设置缓存值
        localCache.putObject(key, EXECUTION_PLACEHOLDER);
        try {

            //获取查询结果
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {

            //移除缓存中的数据
            localCache.removeObject(key);
        }

        //设置缓存中的数据
        localCache.putObject(key, list);

        //处理CALLABLE情况
        if (ms.getStatementType() == StatementType.CALLABLE) {
            localOutputParameterCache.putObject(key, parameter);
        }

        //返回结果
        return list;
    }

    /**
     * 获取链接
     *
     * @param statementLog
     * @return
     * @throws SQLException
     */
    protected Connection getConnection(Log statementLog) throws SQLException {

        //获取链接
        Connection connection = transaction.getConnection();

        if (statementLog.isDebugEnabled()) {

            return ConnectionLogger.newInstance(connection, statementLog, queryStack);
        } else {

            return connection;
        }
    }

    @Override
    public void setExecutorWrapper(Executor wrapper) {
        this.wrapper = wrapper;
    }

    /**
     *
     */
    private static class DeferredLoad {

        /**
         *
         */
        private final MetaObject resultObject;

        /**
         *
         */
        private final String property;

        /**
         *
         */
        private final Class<?> targetType;

        /**
         *
         */
        private final CacheKey key;

        /**
         *
         */
        private final PerpetualCache localCache;

        /**
         *
         */
        private final ObjectFactory objectFactory;

        /**
         *
         */
        private final ResultExtractor resultExtractor;

        // issue #781

        /**
         * @param resultObject
         * @param property
         * @param key
         * @param localCache
         * @param configuration
         * @param targetType
         */
        public DeferredLoad(MetaObject resultObject,
                            String property,
                            CacheKey key,
                            PerpetualCache localCache,
                            Configuration configuration,
                            Class<?> targetType) {
            this.resultObject = resultObject;
            this.property = property;
            this.key = key;
            this.localCache = localCache;
            this.objectFactory = configuration.getObjectFactory();
            this.resultExtractor = new ResultExtractor(configuration, objectFactory);
            this.targetType = targetType;
        }

        /**
         * @return
         */
        public boolean canLoad() {
            return localCache.getObject(key) != null && localCache.getObject(key) != EXECUTION_PLACEHOLDER;
        }

        /**
         *
         */
        public void load() {
            @SuppressWarnings("unchecked")
            // we suppose we get back a List
                    List<Object> list = (List<Object>) localCache.getObject(key);
            Object value = resultExtractor.extractObjectFromList(list, targetType);
            resultObject.setValue(property, value);
        }

    }

}
