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
package org.apache.ibatis.mapping;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.builder.InitializingObject;
import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.decorators.LoggingCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.ScheduledCache;
import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.SynchronizedCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

/**
 * 缓存创建类
 *
 * @author Clinton Begin
 */
public class CacheBuilder {

    /**
     * 缓存构造器的标识，其实就是mapper的命名空间名称
     */
    private final String id;

    /**
     * 缓存实现类对象
     */
    private Class<? extends Cache> implementation;

    /**
     * 缓存策略类对象
     */
    private final List<Class<? extends Cache>> decorators;

    /**
     * 该缓存对象最大存储的数据量
     */
    private Integer size;

    /**
     * 刷新时间间隔
     */
    private Long clearInterval;

    /**
     * 该缓存对象是否是可读写的
     */
    private boolean readWrite;

    /**
     * 该缓存对象所具有的配置参数
     */
    private Properties properties;

    /**
     * 读取数据的时候是否阻塞
     * 它保证只有一个线程到数据库中查找指定key对应的数据
     */
    private boolean blocking;

    public CacheBuilder(String id) {
        this.id = id;
        this.decorators = new ArrayList<Class<? extends Cache>>();
    }

    public CacheBuilder implementation(Class<? extends Cache> implementation) {
        this.implementation = implementation;
        return this;
    }

    public CacheBuilder addDecorator(Class<? extends Cache> decorator) {
        if (decorator != null) {
            this.decorators.add(decorator);
        }
        return this;
    }

    public CacheBuilder size(Integer size) {
        this.size = size;
        return this;
    }

    public CacheBuilder clearInterval(Long clearInterval) {
        this.clearInterval = clearInterval;
        return this;
    }

    public CacheBuilder readWrite(boolean readWrite) {
        this.readWrite = readWrite;
        return this;
    }

    public CacheBuilder blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    public CacheBuilder properties(Properties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * 构建缓存对象
     *
     * @return  构建成功的缓存对象
     */
    public Cache build() {

        //处理默认的设置
        setDefaultImplementations();

        //创建缓存对象
        Cache cache = newBaseCacheInstance(implementation, id);

        //设置缓存的参数
        setCacheProperties(cache);

        // issue #352, do not apply decorators to custom caches
        //如果缓存对象就是PerpetualCache对象
        if (PerpetualCache.class.equals(cache.getClass())) {

            //遍历缓存策略
            for (Class<? extends Cache> decorator : decorators) {

                //使用策略对象对缓存对象进行包装
                cache = newCacheDecoratorInstance(decorator, cache);

                //设置参数
                setCacheProperties(cache);
            }

            //处理其他的数据
            cache = setStandardDecorators(cache);
        } else if (!LoggingCache.class.isAssignableFrom(cache.getClass())) {
            //如果不是LoggingCache， 就使用LoggingCache对其进行包装

            cache = new LoggingCache(cache);
        }

        //返回创建成功的缓存对象
        return cache;
    }

    /**
     * 设置默认的缓存实现类
     */
    private void setDefaultImplementations() {

        //如果缓存实现类为空， 就设置默认的缓存实现类
        if (implementation == null) {

            //默认的缓存实现类
            implementation = PerpetualCache.class;

            //如果缓存策略为空
            if (decorators.isEmpty()) {

                //增加默认的缓存策略，即最近最少使用策略
                decorators.add(LruCache.class);
            }
        }
    }

    /**
     * 处理其他的数据
     *
     * @param cache 缓存对象
     * @return      处理之后的缓存对象
     */
    private Cache setStandardDecorators(Cache cache) {
        try {

            //获取元对象
            MetaObject metaCache = SystemMetaObject.forObject(cache);

            //如果size属性不为空，而且缓存对象有size的设置方法
            if (size != null && metaCache.hasSetter("size")) {

                //设置size
                metaCache.setValue("size", size);
            }

            //如果刷新间隔时间不为空
            if (clearInterval != null) {

                //使用调度缓存包装缓存对象
                cache = new ScheduledCache(cache);

                //设置间隔时间
                ((ScheduledCache) cache).setClearInterval(clearInterval);
            }

            //如果是可读写的
            if (readWrite) {

                //使用序列化缓存包装缓存对象
                cache = new SerializedCache(cache);
            }

            //使用日志缓存包装缓存对象
            cache = new LoggingCache(cache);

            //使用同步缓存包装缓存对象
            cache = new SynchronizedCache(cache);

            //如果是阻塞的
            if (blocking) {

                //使用blocking缓存包装缓存对象
                cache = new BlockingCache(cache);
            }

            //返回缓存对象
            return cache;
        } catch (Exception e) {
            throw new CacheException("Error building standard cache decorators.  Cause: " + e, e);
        }
    }

    /**
     * 设置缓存对象的参数
     *
     * @param cache     缓存对象
     */
    private void setCacheProperties(Cache cache) {

        //如果配置对象不为空
        if (properties != null) {

            //获取缓存对象的元对象
            MetaObject metaCache = SystemMetaObject.forObject(cache);

            //遍历配置对象的值
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {

                //参数名
                String name = (String) entry.getKey();

                //参数值
                String value = (String) entry.getValue();

                //如果缓存对象的元对象有该参数名的设置方法就处理
                if (metaCache.hasSetter(name)) {

                    //获得该参数名的参数值的类型
                    Class<?> type = metaCache.getSetterType(name);

                    //判断类型并转换值
                    if (String.class == type) {
                        //String 类型

                        metaCache.setValue(name, value);
                    } else if (int.class == type
                            || Integer.class == type) {
                        //Integer或者int类型

                        metaCache.setValue(name, Integer.valueOf(value));
                    } else if (long.class == type
                            || Long.class == type) {
                        //Long或者long类型

                        metaCache.setValue(name, Long.valueOf(value));
                    } else if (short.class == type
                            || Short.class == type) {
                        //Short或者short类型

                        metaCache.setValue(name, Short.valueOf(value));
                    } else if (byte.class == type
                            || Byte.class == type) {
                        //Byte或者byte类型

                        metaCache.setValue(name, Byte.valueOf(value));
                    } else if (float.class == type
                            || Float.class == type) {
                        //Float或者float类型

                        metaCache.setValue(name, Float.valueOf(value));
                    } else if (boolean.class == type
                            || Boolean.class == type) {
                        //Boolean或者boolean类型

                        metaCache.setValue(name, Boolean.valueOf(value));
                    } else if (double.class == type
                            || Double.class == type) {
                        //Double或者double类型

                        metaCache.setValue(name, Double.valueOf(value));
                    } else {
                        //其他类型就抛出异常

                        throw new CacheException("Unsupported property type for cache: '" + name + "' of type " + type);
                    }
                }
            }
        }

        //如果该缓存对象实现了InitializingObject接口
        //就调用其initialize方法
        if (InitializingObject.class.isAssignableFrom(cache.getClass())) {
            try {

                //调用initialize方法
                ((InitializingObject) cache).initialize();
            } catch (Exception e) {
                throw new CacheException("Failed cache initialization for '" +
                        cache.getId() + "' on '" + cache.getClass().getName() + "'", e);
            }
        }
    }

    /**
     * 创建缓存对象
     *
     * @param cacheClass    缓存的实现类
     * @param id            命名空间
     * @return              创建的命名空间
     */
    private Cache newBaseCacheInstance(Class<? extends Cache> cacheClass, String id) {

        //获取缓存实现类的构造器对象
        Constructor<? extends Cache> cacheConstructor = getBaseCacheConstructor(cacheClass);
        try {

            //创建并返回缓存对象
            return cacheConstructor.newInstance(id);
        } catch (Exception e) {
            throw new CacheException("Could not instantiate cache implementation (" + cacheClass + "). Cause: " + e, e);
        }
    }

    /**
     * 获取具有String参数的缓存构造器对象
     *
     * @param cacheClass    缓存实现类
     * @return              缓存实现类的构造器对象
     */
    private Constructor<? extends Cache> getBaseCacheConstructor(Class<? extends Cache> cacheClass) {
        try {

            //获取缓存对象拥有一个String参数的构造器对象
            return cacheClass.getConstructor(String.class);
        } catch (Exception e) {
            throw new CacheException("Invalid base cache implementation (" + cacheClass + ").  " +
                    "Base cache implementations must have a constructor that takes a String id as a parameter.  Cause: " + e, e);
        }
    }

    /**
     * 创建包装当前缓存对象的缓存对象
     *
     * @param cacheClass    缓存策略对象
     * @param base          基础缓存对象
     * @return              包装之后的缓存对象
     */
    private Cache newCacheDecoratorInstance(Class<? extends Cache> cacheClass, Cache base) {

        //获取有一个Cache参数的构造器
        Constructor<? extends Cache> cacheConstructor = getCacheDecoratorConstructor(cacheClass);
        try {

            //创建并返回对象
            return cacheConstructor.newInstance(base);
        } catch (Exception e) {
            throw new CacheException("Could not instantiate cache decorator (" + cacheClass + "). Cause: " + e, e);
        }
    }

    /**
     * 获取具有Cache参数的缓存构造器对象
     *
     * @param cacheClass    缓存对象
     * @return              缓存对象的构造器
     */
    private Constructor<? extends Cache> getCacheDecoratorConstructor(Class<? extends Cache> cacheClass) {
        try {

            //返回具有Cache参数的缓存构造器对象
            return cacheClass.getConstructor(Cache.class);
        } catch (Exception e) {
            throw new CacheException("Invalid cache decorator (" + cacheClass + ").  " +
                    "Cache decorators must have a constructor that takes a Cache instance as a parameter.  Cause: " + e, e);
        }
    }
}
