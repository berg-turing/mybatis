package com.berg.base.xmlconfigures.plugins;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.sql.Connection;
import java.util.Properties;

/**
 * 自定义mybatis插件
 */
@Intercepts(
        @Signature(type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class})
)
public class MyPlugins implements Interceptor {

    Properties properties = null;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        System.out.println("插件在方法调用前执行的功能...");

        System.out.println("使用properties对象中的参数" +
                (null == this.properties ? "没有参数" : this.properties.toString()));

        //调用实体方法，如果当前代理对象是一个代理对象的代理对象，那么就是调用
        //下一个代理对象，直到调用到实体方法
        Object result = invocation.proceed();

        System.out.println("插件在方法调用后执行的功能...");

        //返回结果
        return result;
    }

    @Override
    public Object plugin(Object target) {
        //使用mybatis提供的工具创建代理对象
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
