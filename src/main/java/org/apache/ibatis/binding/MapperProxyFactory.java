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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.session.SqlSession;

/**
 * Mapper接口的代理工厂
 * 每一个mapper接口都会有一个代理工厂
 *
 * @author Lasse Voss
 */
public class MapperProxyFactory<T> {

    /**
     * 接口类型
     */
    private final Class<T> mapperInterface;

    /**
     * 对mapper接口内的方法的缓存
     */
    private final Map<Method, MapperMethod> methodCache = new ConcurrentHashMap<Method, MapperMethod>();

    /**
     * 构造函数
     * 创建一个关联了接口的mapper代理工厂对象
     *
     * @param mapperInterface       mapper接口对象
     */
    public MapperProxyFactory(Class<T> mapperInterface) {

        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {

        return mapperInterface;
    }

    /**
     * @return
     */
    public Map<Method, MapperMethod> getMethodCache() {

        return methodCache;
    }

    /**
     * 获取一个动态代理实例
     *
     * @param mapperProxy       接口代理类对象
     * @return                  创建成功的动态代理实例
     */
    @SuppressWarnings("unchecked")
    protected T newInstance(MapperProxy<T> mapperProxy) {

        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }

    /**
     * 获取一个动态代理实例
     *
     * @param sqlSession    sqlSession对象
     * @return              生成的动态代理实例
     */
    public T newInstance(SqlSession sqlSession) {

        final MapperProxy<T> mapperProxy = new MapperProxy<T>(sqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }

}
