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
package org.apache.ibatis.binding;

import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Lasse Voss
 */
public class MapperRegistry {

    /**
     * 配置对象
     */
    private final Configuration config;

    /**
     * mapper接口与其对应的Mapper代理工厂的map
     */
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();


    public MapperRegistry(Configuration config) {

        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {

        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {

        return knownMappers.containsKey(type);

    }

    /**
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type) {

        //判断type是否为接口
        if (type.isInterface()) {

            if (hasMapper(type)) {

                //mapper已经注册
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }

            //mapper是否加载编译完成
            boolean loadCompleted = false;

            try {

                //注册mapper, 使用的接口类型来注册的
                knownMappers.put(type,
                        //mapper代理工厂对象
                        new MapperProxyFactory<T>(type));

                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                //在解析mapper之前将mapper注册是非常重要的,因为如果不提前注册mapper的话,可能多次解析同一个mapper文件
                //而提前标识已经注册了,就只会解析一次
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();

                //标识mapper已经加载编译完成
                loadCompleted = true;

            } finally {

                //如果mapper没有加载编译完成就需要移除注册记录
                if (!loadCompleted) {

                    knownMappers.remove(type);
                }
            }
        }
    }

    /**
     * @since 3.2.2
     */
    public Collection<Class<?>> getMappers() {

        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    /**
     * @since 3.2.2
     * <p>
     * 将指定的包下的所有mapper资源加载
     */
    public void addMappers(String packageName, Class<?> superType) {

        //找到该包下面所有的mapper接口
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();

        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
     * @since 3.2.2
     */
    public void addMappers(String packageName) {

        addMappers(packageName, Object.class);
    }

}
