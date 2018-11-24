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
package org.apache.ibatis.executor;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * 执行器的接口
 *
 * @author Clinton Begin
 */
public interface Executor {

    /**
     * 没有结果处理器的默认值
     */
    ResultHandler NO_RESULT_HANDLER = null;

    /**
     * 更新数据
     *
     * @param ms        MappedStatement对象
     * @param parameter 处理之后的参数对象
     * @return 处理结果影响的数据的条数
     * @throws SQLException SQL语句执行过程中的异常
     */
    int update(MappedStatement ms, Object parameter) throws SQLException;

    /**
     * 执行普通SQL查询数据
     *
     * @param ms            MappedStatement对象
     * @param parameter     处理之后的参数对象
     * @param rowBounds     分页对象
     * @param resultHandler 结果处理器对象
     * @param cacheKey      缓存的key
     * @param boundSql      BoundSql对象
     * @param <E>           当前操作的数据的泛型
     * @return 查询结果
     * @throws SQLException SQL语句执行过程中的异常
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                      CacheKey cacheKey, BoundSql boundSql) throws SQLException;

    /**
     * 执行普通SQL查询数据
     *
     * @param ms            MappedStatement对象
     * @param parameter     处理之后的参数对象
     * @param rowBounds     分页对象
     * @param resultHandler 结果处理器对象
     * @param <E>           当前操作的数据的泛型
     * @return 查询结果
     * @throws SQLException SQL语句执行过程中的异常
     */
    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    /**
     * 执行Cursor查询
     *
     * @param ms        MappedStatement对象
     * @param parameter 处理之后的参数对象
     * @param rowBounds 分页对象
     * @param <E>       当前操作的数据的泛型
     * @return 查询结果
     * @throws SQLException SQL语句执行过程中的异常
     */
    <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

    /**
     * 刷新statement
     *
     * @return 批处理的结果
     * @throws SQLException SQL执行异常
     */
    List<BatchResult> flushStatements() throws SQLException;

    /**
     * 提交事务
     *
     * @param required 是否需要提交事务
     * @throws SQLException SQL执行异常
     */
    void commit(boolean required) throws SQLException;

    /**
     * 回滚事务
     *
     * @param required 是否需要回滚事务
     * @throws SQLException SQL执行异常
     */
    void rollback(boolean required) throws SQLException;

    /**
     * 创建缓存key
     *
     * @param ms              MappedStatement对象
     * @param parameterObject 处理之后的参数对象
     * @param rowBounds       分页对象
     * @param boundSql        BoundSql对象
     * @return 生成成功的缓存key对象
     */
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    /**
     * 判断当前MappedStatement是否已经缓存
     *
     * @param ms  MappedStatement对象
     * @param key 缓存key
     * @return 判断结果
     */
    boolean isCached(MappedStatement ms, CacheKey key);

    /**
     * 清理本地缓存
     */
    void clearLocalCache();

    /**
     * @param ms
     * @param resultObject
     * @param property
     * @param key
     * @param targetType
     */
    void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

    /**
     * 获取事务对象
     *
     * @return 事务对象
     */
    Transaction getTransaction();

    /**
     * 关闭执行器
     *
     * @param forceRollback 是否强制回滚
     */
    void close(boolean forceRollback);

    /**
     * 判断是否已经关闭
     *
     * @return 是否关闭的结果
     */
    boolean isClosed();

    /**
     * 设置执行器对象的包装对象
     *
     * @param executor 包装对象
     */
    void setExecutorWrapper(Executor executor);

}
