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
package org.apache.ibatis.scripting.defaults;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class DefaultParameterHandler implements ParameterHandler {

    /**
     * 类型处理器注册对象
     */
    private final TypeHandlerRegistry typeHandlerRegistry;

    /**
     * MappedStatement对象
     */
    private final MappedStatement mappedStatement;

    /**
     * 参数对象
     */
    private final Object parameterObject;

    /**
     * Sql语句对象
     */
    private final BoundSql boundSql;

    /**
     * 配置对象
     */
    private final Configuration configuration;

    /**
     * DefaultParameterHandler构造函数
     *
     * @param mappedStatement       MappedStatement对象
     * @param parameterObject       参数对象
     * @param boundSql              Sql语句对象
     */
    public DefaultParameterHandler(MappedStatement mappedStatement,
                                   Object parameterObject,
                                   BoundSql boundSql) {

        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public Object getParameterObject() {

        return parameterObject;
    }

    @Override
    public void setParameters(PreparedStatement ps) {

        ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());

        //获取参数映射对象集合
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        //判断是否有参数的映射
        if (parameterMappings != null) {

            //映射参数
            for (int i = 0; i < parameterMappings.size(); i++) {

                //参数映射对象
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {

                    Object value;
                    String propertyName = parameterMapping.getProperty();

                    // issue #448 ask first for additional params
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        //如果参数中有附件的值满足条件，就使用附加值

                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (parameterObject == null) {
                        //如果参数对象为空，则值为空

                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        //如果参数对象有对应的类型处理器，值就为该参数对象

                        value = parameterObject;
                    } else {
                        //否则就从参数对象中寻找对应的属性字段

                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(propertyName);
                    }

                    //获取当前设置的参数的类型处理器
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();

                    //获取当前设置的参数的JdbcType
                    JdbcType jdbcType = parameterMapping.getJdbcType();

                    //如果参数值为null，并且JdbcType也为null
                    if (value == null && jdbcType == null) {

                        //
                        jdbcType = configuration.getJdbcTypeForNull();
                    }

                    try {
                        //设置参数
                        typeHandler.setParameter(ps, i + 1, value, jdbcType);
                    } catch (TypeException e) {
                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                    } catch (SQLException e) {
                        throw new TypeException("Could not set parameters for mapping: " + parameterMapping + ". Cause: " + e, e);
                    }
                }
            }
        }
    }

}
