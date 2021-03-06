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
package org.apache.ibatis.session.defaults;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.result.DefaultMapResultHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

/**
 * The default implementation for {@link SqlSession}.
 * Note that this class is not Thread-Safe.
 *
 * 该类是{@link SqlSession}接口的默认实现
 * 注意，这个类并不是一个线程安全的类
 *
 * @author Clinton Begin
 */
public class DefaultSqlSession implements SqlSession {

    /**
     * 配置对象
     */
    private final Configuration configuration;

    /**
     * 执行器
     */
    private final Executor executor;

    /**
     * 是否自动提交事务
     */
    private final boolean autoCommit;

    /**
     *
     */
    private boolean dirty;

    /**
     *
     */
    private List<Cursor<?>> cursorList;

    /**
     * DefaultSqlSession的构造方法
     *
     * @param configuration     配置对象
     * @param executor          执行器
     * @param autoCommit        是否自动提交
     */
    public DefaultSqlSession(Configuration configuration, Executor executor, boolean autoCommit) {
        this.configuration = configuration;
        this.executor = executor;
        this.dirty = false;
        this.autoCommit = autoCommit;
    }

    /**
     * DefaultSqlSession的构造方法
     *
     * @param configuration     配置对象
     * @param executor          执行器
     */
    public DefaultSqlSession(Configuration configuration, Executor executor) {
        //默认自动提交为false
        this(configuration, executor, false);
    }

    @Override
    public <T> T selectOne(String statement) {
        return this.<T>selectOne(statement, null);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        // Popular vote was to return null on 0 results and throw exception on too many.
        List<T> list = this.<T>selectList(statement, parameter);

        if (list.size() == 1) {
            //查找到的数据只有一条，就返回该条数据

            return list.get(0);
        } else if (list.size() > 1) {
            //如果有多条数据就抛出异常

            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            //如果没有找到数据，就返回null

            return null;
        }
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement,
                                      String mapKey) {

        return this.selectMap(
                statement,
                null,
                mapKey,
                RowBounds.DEFAULT);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement,
                                      Object parameter,
                                      String mapKey) {

        return this.selectMap(
                statement,
                parameter,
                mapKey,
                RowBounds.DEFAULT);
    }

    @Override
    public <K, V> Map<K, V> selectMap(String statement,
                                      Object parameter,
                                      String mapKey,
                                      RowBounds rowBounds) {

        final List<? extends V> list = selectList(statement, parameter, rowBounds);

        final DefaultMapResultHandler<K, V> mapResultHandler =
                new DefaultMapResultHandler<K, V>(
                        mapKey,
                        configuration.getObjectFactory(),
                        configuration.getObjectWrapperFactory(),
                        configuration.getReflectorFactory());

        final DefaultResultContext<V> context = new DefaultResultContext<V>();

        for (V o : list) {
            context.nextResultObject(o);
            mapResultHandler.handleResult(context);
        }

        return mapResultHandler.getMappedResults();
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement) {

        return selectCursor(statement, null);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement,
                                      Object parameter) {

        return selectCursor(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <T> Cursor<T> selectCursor(String statement,
                                      Object parameter,
                                      RowBounds rowBounds) {

        //所有的selectCursor都集中在了该方法中

        try {
            MappedStatement ms = configuration.getMappedStatement(statement);

            Cursor<T> cursor = executor.queryCursor(ms, wrapCollection(parameter), rowBounds);

            registerCursor(cursor);

            return cursor;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public <E> List<E> selectList(String statement) {

        return this.selectList(statement, null);
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {

        return this.selectList(statement, parameter, RowBounds.DEFAULT);
    }

    @Override
    public <E> List<E> selectList(String statement,
                                  Object parameter,
                                  RowBounds rowBounds) {
        //所有的普通查询最终都集中在了该查询中
        //  无论是
        //      selectOne
        //      selectMap
        //      selectLst

        try {

            //获取mappedStatement对象
            MappedStatement ms = configuration.getMappedStatement(statement);

            //执行并返回结果
            return executor.query(
                    ms,
                    wrapCollection(parameter),
                    rowBounds,
                    Executor.NO_RESULT_HANDLER);
        } catch (Exception e) {

            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {

            ErrorContext.instance().reset();
        }
    }

    @Override
    public void select(String statement,
                       Object parameter,
                       ResultHandler handler) {

        select(statement, parameter, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement,
                       ResultHandler handler) {

        select(statement, null, RowBounds.DEFAULT, handler);
    }

    @Override
    public void select(String statement,
                       Object parameter,
                       RowBounds rowBounds,
                       ResultHandler handler) {

        try {

            MappedStatement ms = configuration.getMappedStatement(statement);

            executor.query(
                    ms,
                    wrapCollection(parameter),
                    rowBounds,
                    handler);
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error querying database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public int insert(String statement) {

        return insert(statement, null);
    }

    @Override
    public int insert(String statement, Object parameter) {

        return update(statement, parameter);
    }

    @Override
    public int update(String statement) {

        return update(statement, null);
    }

    @Override
    public int update(String statement, Object parameter) {

        try {
            //更新之后，数据就变脏了
            dirty = true;

            MappedStatement ms = configuration.getMappedStatement(statement);

            return executor.update(ms, wrapCollection(parameter));
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error updating database.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public int delete(String statement) {

        return update(statement, null);
    }

    @Override
    public int delete(String statement,
                      Object parameter) {

        return update(statement, parameter);
    }

    @Override
    public void commit() {
        commit(false);
    }

    @Override
    public void commit(boolean force) {

        try {
            //提交事务
            executor.commit(isCommitOrRollbackRequired(force));

            //不脏了
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error committing transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void rollback() {
        rollback(false);
    }

    @Override
    public void rollback(boolean force) {

        try {

            //回滚
            executor.rollback(isCommitOrRollbackRequired(force));

            //不脏了
            dirty = false;
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public List<BatchResult> flushStatements() {
        try {

            return executor.flushStatements();
        } catch (Exception e) {
            throw ExceptionFactory.wrapException("Error flushing statements.  Cause: " + e, e);
        } finally {
            ErrorContext.instance().reset();
        }
    }

    @Override
    public void close() {
        try {

            executor.close(isCommitOrRollbackRequired(false));
            closeCursors();
            dirty = false;
        } finally {
            ErrorContext.instance().reset();
        }
    }

    private void closeCursors() {

        if (cursorList != null && cursorList.size() != 0) {
            for (Cursor<?> cursor : cursorList) {
                try {
                    cursor.close();
                } catch (IOException e) {
                    throw ExceptionFactory.wrapException("Error closing cursor.  Cause: " + e, e);
                }
            }
            cursorList.clear();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.<T>getMapper(type, this);
    }

    @Override
    public Connection getConnection() {
        try {
            return executor.getTransaction().getConnection();
        } catch (SQLException e) {
            throw ExceptionFactory.wrapException("Error getting a new connection.  Cause: " + e, e);
        }
    }

    @Override
    public void clearCache() {
        executor.clearLocalCache();
    }

    /**
     * 注册游标
     *
     * @param cursor    游标对象
     * @param <T>       结果值的泛型
     */
    private <T> void registerCursor(Cursor<T> cursor) {

        if (cursorList == null) {
            cursorList = new ArrayList<Cursor<?>>();
        }

        cursorList.add(cursor);
    }

    /**
     * 是否有提交或者回滚的需要
     *
     * @param force     是否强制性要求
     * @return          返回判断结果
     */
    private boolean isCommitOrRollbackRequired(boolean force) {

        return (!autoCommit && dirty) || force;
    }

    /**
     * 包装集合对象
     *
     * @param object    传入的对象
     * @return          包装之后的对象
     */
    private Object wrapCollection(final Object object) {

        if (object instanceof Collection) {
            StrictMap<Object> map = new StrictMap<Object>();

            map.put("collection", object);

            if (object instanceof List) {

                map.put("list", object);
            }

            return map;
        } else if (object != null && object.getClass().isArray()) {

            StrictMap<Object> map = new StrictMap<Object>();
            map.put("array", object);

            return map;
        }

        return object;
    }

    public static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -5741767162221585340L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
            }
            return super.get(key);
        }

    }

}
