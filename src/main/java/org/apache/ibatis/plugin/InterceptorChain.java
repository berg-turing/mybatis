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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Clinton Begin
 */

/**
 * 插件链对象
 */
public class InterceptorChain {

    /**
     * 插件List
     */
    private final List<Interceptor> interceptors = new ArrayList<Interceptor>();

    /**
     * 执行所有插件的plugin方法
     *
     * @param target
     * @return
     */
    public Object pluginAll(Object target) {

        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }

        return target;
    }

    /**
     * 增加一个插件到插件list对象中
     *
     * @param interceptor
     */
    public void addInterceptor(Interceptor interceptor) {

        interceptors.add(interceptor);
    }

    /**
     * 获取插件list对象
     *
     * @return
     */
    public List<Interceptor> getInterceptors() {

        return Collections.unmodifiableList(interceptors);
    }

}
