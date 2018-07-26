/**
 * Copyright 2009-2017 the original author or authors.
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

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * 带缓存的执行器
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class CachingExecutor implements Executor {

    /**
     * 代理的执行器对象
     */
    private final Executor delegate;

    /**
     *
     */
    private final TransactionalCacheManager tcm = new TransactionalCacheManager();

    /**
     * 带缓存的执行器的构造器
     *
     * @param delegate 实际的执行器对象
     */
    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;

        //设置该执行器的执行器包装对象
        delegate.setExecutorWrapper(this);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            //issues #499, #524 and #573
            if (forceRollback) {

                //回滚
                tcm.rollback();
            } else {

                //提交
                tcm.commit();
            }
        } finally {

            //关闭执行器
            delegate.close(forceRollback);
        }
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public int update(MappedStatement ms, Object parameterObject) throws SQLException {

        //是否刷新缓存
        flushCacheIfRequired(ms);

        //更新数据
        return delegate.update(ms, parameterObject);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {

        //获取Sql对象
        BoundSql boundSql = ms.getBoundSql(parameterObject);

        //创建缓存key
        CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);

        //返回查询寻结果
        return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        flushCacheIfRequired(ms);
        return delegate.queryCursor(ms, parameter, rowBounds);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
            throws SQLException {

        //获取缓存对象
        Cache cache = ms.getCache();

        if (cache != null) {

            //判断是否需要刷新缓存
            flushCacheIfRequired(ms);


            if (ms.isUseCache() && resultHandler == null) {

                //
                ensureNoOutParams(ms, parameterObject, boundSql);

                //
                @SuppressWarnings("unchecked")
                List<E> list = (List<E>) tcm.getObject(cache, key);

                if (list == null) {

                    list = delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);

                    tcm.putObject(cache, key, list); // issue #578 and #116
                }

                return list;
            }
        }

        return delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return delegate.flushStatements();
    }

    @Override
    public void commit(boolean required) throws SQLException {
        delegate.commit(required);
        tcm.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        try {
            delegate.rollback(required);
        } finally {
            if (required) {
                tcm.rollback();
            }
        }
    }

    private void ensureNoOutParams(MappedStatement ms, Object parameter, BoundSql boundSql) {
        if (ms.getStatementType() == StatementType.CALLABLE) {
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                if (parameterMapping.getMode() != ParameterMode.IN) {
                    throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
                }
            }
        }
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {

        //
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public boolean isCached(MappedStatement ms, CacheKey key) {
        return delegate.isCached(ms, key);
    }

    @Override
    public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
        delegate.deferLoad(ms, resultObject, property, key, targetType);
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    /**
     * 如果需要刷新缓存，就刷新缓存
     *
     * @param ms
     */
    private void flushCacheIfRequired(MappedStatement ms) {

        //获取缓存对象
        Cache cache = ms.getCache();

        //判断是否需要刷新缓存
        if (cache != null && ms.isFlushCacheRequired()) {

            //刷新缓存
            tcm.clear(cache);
        }
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

}
