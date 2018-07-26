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
package org.apache.ibatis.type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;

/**
 * @author Clinton Begin
 */

/**
 * 类型别名注册器
 */
public class TypeAliasRegistry {

    /**
     * 存放类型别名的Map对象
     */
    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<String, Class<?>>();

    /**
     * 构造并初始化默认的类型别名
     * <p>
     * 对象类型的别名都是其类名首字母小写的字符串
     * 基本类型的别名是在基本类型类型名前面加"_"的字符串
     */
    public TypeAliasRegistry() {

        //字符串类型
        registerAlias("string", String.class);

        //八大基本类型的包装类
        registerAlias("byte", Byte.class);
        registerAlias("long", Long.class);
        registerAlias("short", Short.class);
        registerAlias("int", Integer.class);
        registerAlias("integer", Integer.class);
        registerAlias("double", Double.class);
        registerAlias("float", Float.class);
        registerAlias("boolean", Boolean.class);

        //八大基本类型的包装类数组
        registerAlias("byte[]", Byte[].class);
        registerAlias("long[]", Long[].class);
        registerAlias("short[]", Short[].class);
        registerAlias("int[]", Integer[].class);
        registerAlias("integer[]", Integer[].class);
        registerAlias("double[]", Double[].class);
        registerAlias("float[]", Float[].class);
        registerAlias("boolean[]", Boolean[].class);

        //八大基本类型
        registerAlias("_byte", byte.class);
        registerAlias("_long", long.class);
        registerAlias("_short", short.class);
        registerAlias("_int", int.class);
        registerAlias("_integer", int.class);
        registerAlias("_double", double.class);
        registerAlias("_float", float.class);
        registerAlias("_boolean", boolean.class);

        //八大基本类型数组
        registerAlias("_byte[]", byte[].class);
        registerAlias("_long[]", long[].class);
        registerAlias("_short[]", short[].class);
        registerAlias("_int[]", int[].class);
        registerAlias("_integer[]", int[].class);
        registerAlias("_double[]", double[].class);
        registerAlias("_float[]", float[].class);
        registerAlias("_boolean[]", boolean[].class);

        //其他常用类型
        registerAlias("date", Date.class);
        registerAlias("decimal", BigDecimal.class);
        registerAlias("bigdecimal", BigDecimal.class);
        registerAlias("biginteger", BigInteger.class);
        registerAlias("object", Object.class);

        //其他常用类型数组
        registerAlias("date[]", Date[].class);
        registerAlias("decimal[]", BigDecimal[].class);
        registerAlias("bigdecimal[]", BigDecimal[].class);
        registerAlias("biginteger[]", BigInteger[].class);
        registerAlias("object[]", Object[].class);

        //集合类型
        registerAlias("map", Map.class);
        registerAlias("hashmap", HashMap.class);
        registerAlias("list", List.class);
        registerAlias("arraylist", ArrayList.class);
        registerAlias("collection", Collection.class);
        registerAlias("iterator", Iterator.class);

        //数据集类型
        registerAlias("ResultSet", ResultSet.class);
    }

    @SuppressWarnings("unchecked")
    // throws class cast exception as well if types cannot be assigned
    /**
     * 根据别名获取类型
     *
     * 别名是不区分大小写的
     *
     */
    public <T> Class<T> resolveAlias(String string) {

        try {

            if (string == null) {
                return null;
            }

            // issue #748
            String key = string.toLowerCase(Locale.ENGLISH);

            Class<T> value;

            if (TYPE_ALIASES.containsKey(key)) {

                value = (Class<T>) TYPE_ALIASES.get(key);

            } else {

                value = (Class<T>) Resources.classForName(string);
            }

            return value;

        } catch (ClassNotFoundException e) {

            throw new TypeException("Could not resolve type alias '" + string + "'.  Cause: " + e, e);
        }
    }

    /**
     * 注册别名
     *
     * @param packageName
     */
    public void registerAliases(String packageName) {


        registerAliases(packageName, Object.class);
    }

    /**
     * @param packageName
     * @param superType
     */
    public void registerAliases(String packageName, Class<?> superType) {

        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();

        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);

        Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();

        for (Class<?> type : typeSet) {
            // Ignore inner classes and interfaces (including package-info.java)
            // Skip also inner classes. See issue #6

            if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
                registerAlias(type);
            }
        }
    }

    /**
     * @param type
     */
    public void registerAlias(Class<?> type) {

        //获取别名
        String alias = type.getSimpleName();


        Alias aliasAnnotation = type.getAnnotation(Alias.class);

        //获取注解的别名值, 这里说明注解的值会覆盖配置的值
        if (aliasAnnotation != null) {

            alias = aliasAnnotation.value();
        }

        //注册别名
        registerAlias(alias, type);
    }

    /**
     * @param alias
     * @param value
     */
    public void registerAlias(String alias, Class<?> value) {
        if (alias == null) {
            throw new TypeException("The parameter alias cannot be null");
        }

        // issue #748
        String key = alias.toLowerCase(Locale.ENGLISH);

        //当别名冲突的时候, 会抛出下面的异常
        if (TYPE_ALIASES.containsKey(key) && TYPE_ALIASES.get(key) != null && !TYPE_ALIASES.get(key).equals(value)) {

            throw new TypeException("The alias '" + alias + "' is already mapped to the value '" + TYPE_ALIASES.get(key).getName() + "'.");
        }

        //成功注册别名
        TYPE_ALIASES.put(key, value);
    }

    /**
     * @param alias
     * @param value
     */
    public void registerAlias(String alias, String value) {
        try {
            registerAlias(alias, Resources.classForName(value));

        } catch (ClassNotFoundException e) {
            throw new TypeException("Error registering type alias " + alias + " for " + value + ". Cause: " + e, e);
        }
    }

    /**
     * @since 3.2.2
     */
    public Map<String, Class<?>> getTypeAliases() {
        return Collections.unmodifiableMap(TYPE_ALIASES);
    }

}
