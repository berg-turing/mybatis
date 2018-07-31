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
package org.apache.ibatis.binding;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 */
public class MapperMethod {

    /**
     * sql命令对象
     */
    private final SqlCommand command;

    /**
     * 方法的签名
     */
    private final MethodSignature method;

    /**
     * MapperMethod构造方法
     *
     * @param mapperInterface   mapper接口
     * @param method            方法对象
     * @param config            配置对象
     */
    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {

        //sql command 对象
        this.command = new SqlCommand(config, mapperInterface, method);

        //创建方法签名
        this.method = new MethodSignature(config, mapperInterface, method);
    }

    /**
     * 执行代理的过程
     *
     * @param sqlSession    sqlSession对象
     * @param args          方法执行时候的参数
     * @return              方法执行的结果
     */
    public Object execute(SqlSession sqlSession, Object[] args) {

        Object result;
        switch (command.getType()) {
            //插入数据
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            //更新数据
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            //删除数据
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            //查询数据
            case SELECT:

                if (method.returnsVoid() && method.hasResultHandler()) {
                    //如果该方法返回的结果为空，并且有结果处理器

                    executeWithResultHandler(sqlSession, args);
                    result = null;
                } else if (method.returnsMany()) {
                    //如果返回的是集合

                    result = executeForMany(sqlSession, args);
                } else if (method.returnsMap()) {
                    //如果返回的是map

                    result = executeForMap(sqlSession, args);
                } else if (method.returnsCursor()) {
                    //

                    result = executeForCursor(sqlSession, args);
                } else {
                    //返回的是单个数据

                    Object param = method.convertArgsToSqlCommandParam(args);
                    result = sqlSession.selectOne(command.getName(), param);
                }
                break;
            //刷新语句
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }

        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }

        return result;
    }

    /**
     *
     * @param rowCount
     * @return
     */
    private Object rowCountResult(int rowCount) {

        final Object result;
        if (method.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = rowCount;
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = (long) rowCount;
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    /**
     *
     * @param sqlSession
     * @param args
     */
    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {

        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());

        if (void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException("method " + command.getName()
                    + " needs either a @ResultMap annotation, a @ResultType annotation,"
                    + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }

        Object param = method.convertArgsToSqlCommandParam(args);

        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
        } else {
            sqlSession.select(command.getName(), param, method.extractResultHandler(args));
        }
    }

    /**
     * 处理返回结果是集合的情况
     *
     * @param sqlSession        SqlSession对象
     * @param args              参数对象
     * @param <E>               返回结果的泛型
     * @return                  返回查找到的数据
     */
    private <E> Object executeForMany(SqlSession sqlSession,
                                      Object[] args) {

        //返回结果
        List<E> result;

        //参数对象
        Object param = method.convertArgsToSqlCommandParam(args);

        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<E>selectList(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.<E>selectList(command.getName(), param);
        }

        // issue #510 Collections & arrays support
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
                return convertToArray(result);
            } else {
                return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }

        return result;
    }

    /**
     *
     * @param sqlSession
     * @param args
     * @param <T>
     * @return
     */
    private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {

        Cursor<T> result;

        Object param = method.convertArgsToSqlCommandParam(args);

        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<T>selectCursor(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.<T>selectCursor(command.getName(), param);
        }

        return result;
    }

    /**
     *
     * @param config
     * @param list
     * @param <E>
     * @return
     */
    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {

        Object collection = config.getObjectFactory().create(method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    /**
     *
     * @param list
     * @param <E>
     * @return
     */
    @SuppressWarnings("unchecked")
    private <E> Object convertToArray(List<E> list) {

        Class<?> arrayComponentType = method.getReturnType().getComponentType();
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (arrayComponentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            return list.toArray((E[]) array);
        }
    }

    /**
     * 处理返回值是map的情况
     *
     * @param sqlSession        SqlSession对象
     * @param args              方法的参数
     * @param <K>               map的key泛型
     * @param <V>               map的value的泛型
     * @return                  查询的结果
     */
    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {

        Map<K, V> result;

        Object param = method.convertArgsToSqlCommandParam(args);

        if (method.hasRowBounds()) {

            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.<K, V>selectMap(command.getName(), param, method.getMapKey());
        }

        return result;
    }

    /**
     *
     * @param <V>
     */
    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -2212268410512043556L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }

    }

    /**
     * SqlCommand类
     */
    public static class SqlCommand {

        /**
         * sql命令的名称，全名称
         */
        private final String name;

        /**
         * sql命令的类型
         */
        private final SqlCommandType type;

        /**
         * sql
         *
         * @param configuration         配置对象
         * @param mapperInterface       mapper的接口类
         * @param method                当前执行的mapper方法对象
         */
        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {

            //方法名
            final String methodName = method.getName();

            //定义方法的接口类
            final Class<?> declaringClass = method.getDeclaringClass();

            //获取MappedStatement对象
            MappedStatement ms = resolveMappedStatement(mapperInterface, methodName, declaringClass,
                    configuration);

            //判断是否找到了对应的MappedStatement对象
            if (ms == null) {

                //判断方法是否有 Flush 的注解
                if (method.getAnnotation(Flush.class) != null) {

                    name = null;
                    //sql命令的类型为刷新
                    type = SqlCommandType.FLUSH;
                } else {

                    //没有找到MappedStatement对象，同时该方法也没有Flush注解
                    //就抛出异常
                    throw new BindingException("Invalid bound statement (not found): "
                            + mapperInterface.getName() + "." + methodName);
                }
            } else {

                //获取MappedStatement对象的id
                name = ms.getId();

                //获取MappedStatement对象的命令类型
                type = ms.getSqlCommandType();

                //如果是未知的命令类型，就抛出异常
                if (type == SqlCommandType.UNKNOWN) {
                    throw new BindingException("Unknown execution method for: " + name);
                }
            }
        }

        /**
         * 获取name
         *
         * @return  返回该条sql的name
         */
        public String getName() {
            return name;
        }

        /**
         * 获取该条sql的类型
         *
         * @return  该条sql的类型
         */
        public SqlCommandType getType() {
            return type;
        }

        /**
         * 解析相关的参数，获取对应的MappedStatement对象
         *
         * @param mapperInterface       mapper的接口类
         * @param methodName            方法名
         * @param declaringClass        定义方法的接口类
         * @param configuration         配置对象
         * @return                      MappedStatement对象
         */
        private MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
                                                       Class<?> declaringClass, Configuration configuration) {

            //获取statementId
            String statementId = mapperInterface.getName() + "." + methodName;

            //判断配置对象中是否含有该MappedStatement对象
            if (configuration.hasStatement(statementId)) {

                return configuration.getMappedStatement(statementId);
            } else if (mapperInterface.equals(declaringClass)) {
                //如果接口就是定义该方法的类，就返回null

                return null;
            }

            //获取父接口再重新查询
            for (Class<?> superInterface : mapperInterface.getInterfaces()) {

                //如果定义的类是来自于某个父接口
                if (declaringClass.isAssignableFrom(superInterface)) {
                    //再次递归解析
                    MappedStatement ms = resolveMappedStatement(superInterface, methodName,
                            declaringClass, configuration);

                    //如果找到的MappedStatement对象不为空，就返回该对象
                    if (ms != null) {
                        return ms;
                    }
                }
            }

            //返回null
            return null;
        }
    }

    /**
     * 对执行方法的一个方法签名
     */
    public static class MethodSignature {

        /**
         * 返回值是否是集合
         */
        private final boolean returnsMany;

        /**
         * 返回值是否是map
         */
        private final boolean returnsMap;

        /**
         * 返回值是否是void
         */
        private final boolean returnsVoid;

        /**
         * 返回值是否是Cursor
         */
        private final boolean returnsCursor;

        /**
         * 返回值的类型
         */
        private final Class<?> returnType;

        /**
         *
         */
        private final String mapKey;

        /**
         * resultHandler对象在方法参数中的下标
         */
        private final Integer resultHandlerIndex;

        /**
         * 分页对象在方法参数中的下标
         */
        private final Integer rowBoundsIndex;

        /**
         *
         */
        private final ParamNameResolver paramNameResolver;

        /**
         * 创建方法签名对象
         *
         * @param configuration         配置对象
         * @param mapperInterface       mapper的接口
         * @param method                当前执行的方法
         */
        public MethodSignature(Configuration configuration, Class<?> mapperInterface, Method method) {

            //解析返回值的类型
            Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);

            if (resolvedReturnType instanceof Class<?>) {

                this.returnType = (Class<?>) resolvedReturnType;
            } else if (resolvedReturnType instanceof ParameterizedType) {

                this.returnType = (Class<?>) ((ParameterizedType) resolvedReturnType).getRawType();
            } else {

                this.returnType = method.getReturnType();
            }

            //返回值是否为void
            this.returnsVoid = void.class.equals(this.returnType);
            //返回值是否为集合
            this.returnsMany = (configuration.getObjectFactory().isCollection(this.returnType) || this.returnType.isArray());
            //返回值是否为游标
            this.returnsCursor = Cursor.class.equals(this.returnType);
            //获取map的key
            this.mapKey = getMapKey(method);
            //返回值是否为map
            this.returnsMap = (this.mapKey != null);

            //分页对象参数下标
            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            //结果处理器参数下标
            this.resultHandlerIndex = getUniqueParamIndex(method, ResultHandler.class);
            //参数名解析器
            this.paramNameResolver = new ParamNameResolver(configuration, method);
        }

        /**
         * 将方法参数转换成SqlCommandParam
         *
         * @param args      方法的参数
         * @return          SqlCommandParam对象
         */
        public Object convertArgsToSqlCommandParam(Object[] args) {

            return paramNameResolver.getNamedParams(args);
        }

        /**
         * 判断是否有分页的参数
         *
         * @return  有就返回true，没有就返回false
         */
        public boolean hasRowBounds() {

            return rowBoundsIndex != null;
        }

        /**
         * 获取RowBounds对象
         *
         * @param args      参数对象
         * @return          RowBounds对象
         */
        public RowBounds extractRowBounds(Object[] args) {

            return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        /**
         * 判断是否有结果处理器对象的参数
         *
         * @return  有就返回true， 没有就返回false
         */
        public boolean hasResultHandler() {

            return resultHandlerIndex != null;
        }

        /**
         * 获取ResultHandler对象
         *
         * @param args      参数对象
         * @return          ResultHandler对象
         */
        public ResultHandler extractResultHandler(Object[] args) {

            return hasResultHandler() ? (ResultHandler) args[resultHandlerIndex] : null;
        }

        /**
         * 获取map key
         * @return      mapKey
         */
        public String getMapKey() {

            return mapKey;
        }

        /**
         * 获取返回值类型
         *
         * @return  返回值类型
         */
        public Class<?> getReturnType() {

            return returnType;
        }

        /**
         * 返回值是否为集合
         *
         * @return  是就返回true，否则返回false
         */
        public boolean returnsMany() {

            return returnsMany;
        }

        /**
         * 返回值是否为map
         *
         * @return  是就返回true，否则返回false
         */
        public boolean returnsMap() {

            return returnsMap;
        }

        /**
         * 返回值是否为void
         *
         * @return  是就返回true，否则返回false
         */
        public boolean returnsVoid() {

            return returnsVoid;
        }

        /**
         * 返回值是否为Cursor
         *
         * @return  是就返回true，否则返回false
         */
        public boolean returnsCursor() {

            return returnsCursor;
        }

        /**
         * 获取指定类型的参数在方法中的下标
         *
         * @param method        方法对象
         * @param paramType     参数类型
         * @return              返回参数的下标值，如果没有就返回null
         */
        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {

            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }

            //返回下标
            return index;
        }

        /**
         *
         * @param method
         * @return
         */
        private String getMapKey(Method method) {

            String mapKey = null;
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                final MapKey mapKeyAnnotation = method.getAnnotation(MapKey.class);
                if (mapKeyAnnotation != null) {
                    mapKey = mapKeyAnnotation.value();
                }
            }
            return mapKey;
        }
    }

}
