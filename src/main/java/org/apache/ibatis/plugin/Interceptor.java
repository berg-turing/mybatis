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
package org.apache.ibatis.plugin;

import java.util.Properties;

/**
 * 插件接口
 *
 * @author Clinton Begin
 */
public interface Interceptor {

    /**
     * 当前插件实现的额外的功能方法
     *
     * @param invocation 调用对象，内部包含四个基本对象
     * @return 执行结果
     * @throws Throwable 处理过程中的异常
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 生成插件的代理对象
     *
     * @param target 代理的目标对象
     * @return 生成的代理对象
     */
    Object plugin(Object target);

    /**
     * 设置额外的属性对象
     *
     * @param properties 待设置的属性对象
     */
    void setProperties(Properties properties);

}
