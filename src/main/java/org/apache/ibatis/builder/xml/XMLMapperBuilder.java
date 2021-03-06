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
package org.apache.ibatis.builder.xml;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Clinton Begin
 * <p>
 * mapper文件解析器
 */
public class XMLMapperBuilder extends BaseBuilder {

    /**
     * xml解析对象
     */
    private final XPathParser parser;

    /**
     *
     */
    private final MapperBuilderAssistant builderAssistant;

    /**
     *
     */
    private final Map<String, XNode> sqlFragments;

    /**
     *
     */
    private final String resource;

    @Deprecated
    public XMLMapperBuilder(Reader reader,
                            Configuration configuration,
                            String resource,
                            Map<String, XNode> sqlFragments,
                            String namespace) {

        this(reader, configuration, resource, sqlFragments);

        this.builderAssistant.setCurrentNamespace(namespace);
    }

    @Deprecated
    public XMLMapperBuilder(Reader reader,
                            Configuration configuration,
                            String resource,
                            Map<String, XNode> sqlFragments) {

        this(new XPathParser(reader, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    public XMLMapperBuilder(InputStream inputStream,
                            Configuration configuration,
                            String resource,
                            Map<String, XNode> sqlFragments,
                            String namespace) {

        this(inputStream, configuration, resource, sqlFragments);
        this.builderAssistant.setCurrentNamespace(namespace);
    }

    public XMLMapperBuilder(InputStream inputStream,
                            Configuration configuration,
                            String resource,
                            Map<String, XNode> sqlFragments) {

        this(new XPathParser(inputStream, true, configuration.getVariables(), new XMLMapperEntityResolver()),
                configuration, resource, sqlFragments);
    }

    private XMLMapperBuilder(XPathParser parser,
                             Configuration configuration,
                             String resource,
                             Map<String, XNode> sqlFragments) {

        super(configuration);
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.parser = parser;
        this.sqlFragments = sqlFragments;
        this.resource = resource;
    }

    /**
     *
     *
     */
    public void parse() {

        if (!configuration.isResourceLoaded(resource)) {

            //解析mapper标签的内容
            configurationElement(parser.evalNode("/mapper"));

            configuration.addLoadedResource(resource);

            bindMapperForNamespace();
        }

        parsePendingResultMaps();
        parsePendingCacheRefs();
        parsePendingStatements();
    }

    public XNode getSqlFragment(String refid) {

        return sqlFragments.get(refid);
    }

    /**
     * @param context
     */
    private void configurationElement(XNode context) {

        try {

            //获取命名空间
            String namespace = context.getStringAttribute("namespace");

            //提示命名空间不能为空
            if (namespace == null || namespace.equals("")) {
                throw new BuilderException("Mapper's namespace cannot be empty");
            }

            //设置当前的命名空间
            builderAssistant.setCurrentNamespace(namespace);

            //解析cache-ref
            cacheRefElement(context.evalNode("cache-ref"));

            //解析cache
            cacheElement(context.evalNode("cache"));

            //解析parameterMap
            parameterMapElement(context.evalNodes("/mapper/parameterMap"));

            //解析resultMap
            resultMapElements(context.evalNodes("/mapper/resultMap"));

            //解析sql
            sqlElement(context.evalNodes("/mapper/sql"));

            //解析select|insert|update|delete
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));

        } catch (Exception e) {

            //报mapper解析错误
            throw new BuilderException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    /**
     * 创建Statement对象
     *
     * @param list  节点对象
     */
    private void buildStatementFromContext(List<XNode> list) {

        if (configuration.getDatabaseId() != null) {

            //
            buildStatementFromContext(list, configuration.getDatabaseId());
        }

        buildStatementFromContext(list, null);
    }

    private void buildStatementFromContext(List<XNode> list, String requiredDatabaseId) {
        for (XNode context : list) {
            final XMLStatementBuilder statementParser = new XMLStatementBuilder(configuration, builderAssistant, context, requiredDatabaseId);
            try {
                statementParser.parseStatementNode();
            } catch (IncompleteElementException e) {
                configuration.addIncompleteStatement(statementParser);
            }
        }
    }

    private void parsePendingResultMaps() {
        Collection<ResultMapResolver> incompleteResultMaps = configuration.getIncompleteResultMaps();
        synchronized (incompleteResultMaps) {
            Iterator<ResultMapResolver> iter = incompleteResultMaps.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // ResultMap is still missing a resource...
                }
            }
        }
    }

    private void parsePendingCacheRefs() {
        Collection<CacheRefResolver> incompleteCacheRefs = configuration.getIncompleteCacheRefs();
        synchronized (incompleteCacheRefs) {
            Iterator<CacheRefResolver> iter = incompleteCacheRefs.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolveCacheRef();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Cache ref is still missing a resource...
                }
            }
        }
    }

    private void parsePendingStatements() {
        Collection<XMLStatementBuilder> incompleteStatements = configuration.getIncompleteStatements();
        synchronized (incompleteStatements) {
            Iterator<XMLStatementBuilder> iter = incompleteStatements.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().parseStatementNode();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // Statement is still missing a resource...
                }
            }
        }
    }

    /**
     * 解析<cache-ref></cache-ref>标签
     *
     * @param context cache-ref节点对象
     */
    private void cacheRefElement(XNode context) {

        if (context != null) {

            //增加<cache-ref></cache-ref>标签引用的命名空间之间的对应关系
            configuration.addCacheRef(builderAssistant.getCurrentNamespace(), context.getStringAttribute("namespace"));

            //缓存索引解析器
            CacheRefResolver cacheRefResolver =
                    new CacheRefResolver(builderAssistant, context.getStringAttribute("namespace"));

            try {

                //尝试是否能索引到缓存对象
                cacheRefResolver.resolveCacheRef();

            } catch (IncompleteElementException e) {

                //没有找到索引的缓存对象，将缓存索引解析器先保存
                configuration.addIncompleteCacheRef(cacheRefResolver);
            }
        }
    }

    /**
     * 解析<cache></cache>标签
     *
     * @param context       cache节点对象
     * @throws Exception    无作用
     */
    private void cacheElement(XNode context) throws Exception {

        //节点对象存在就开始解析
        if (context != null) {

            //缓存类型，默认为系统的PERPETUAL
            String type = context.getStringAttribute("type", "PERPETUAL");

            //从类型别名注册器中找到该类型
            Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);

            //获取缓存回收策略类型，默认是LRU(最近最少使用)
            //可以设置的回收策略类型为：
            //          LRU(最近最少使用，移除最长时间不用的对象)
            //          FIFO(先进显先出，按对象进入缓存的顺序来移除它们)
            //          SOFT(软引用，移除基于垃圾回收器状态和软引用规则的对象)
            //          WEAK(弱引用，更积极的移除基于垃圾回收器状态和弱引用规则的对象)
            String eviction = context.getStringAttribute("eviction", "LRU");

            //获取缓存回收策略的类
            Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);

            //刷新时间间隔，单位为毫秒，如果不配置，那么当SQL执行的时候才会去刷新缓存
            Long flushInterval = context.getLongAttribute("flushInterval");

            //引用数目，表示缓存最多可以存储多少个对象
            Integer size = context.getIntAttribute("size");

            //是否是只读，如果是只读，就意味着缓存数据只能读取不能修改，默认是false
            //这里对得到的值进行了取非，属性的含义为读写
            boolean readWrite = !context.getBooleanAttribute("readOnly", false);

            //读取数据的时候是否阻塞
            boolean blocking = context.getBooleanAttribute("blocking", false);

            //获取属性配置的值
            Properties props = context.getChildrenAsProperties();

            //创建并设置缓存对象
            builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
        }
    }

    /**
     * 解析<parameterMap></parameterMap>标签
     *
     * @param list          <parameterMap></parameterMap>标签对象
     * @throws Exception    无
     */
    private void parameterMapElement(List<XNode> list) throws Exception {

        //遍历parameterMap对象
        for (XNode parameterMapNode : list) {

            //
            String id = parameterMapNode.getStringAttribute("id");

            //
            String type = parameterMapNode.getStringAttribute("type");

            //
            Class<?> parameterClass = resolveClass(type);

            //
            List<XNode> parameterNodes = parameterMapNode.evalNodes("parameter");
            //
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();

            //
            for (XNode parameterNode : parameterNodes) {

                //
                String property = parameterNode.getStringAttribute("property");

                //
                String javaType = parameterNode.getStringAttribute("javaType");

                //
                String jdbcType = parameterNode.getStringAttribute("jdbcType");

                //
                String resultMap = parameterNode.getStringAttribute("resultMap");

                //
                String mode = parameterNode.getStringAttribute("mode");

                //
                String typeHandler = parameterNode.getStringAttribute("typeHandler");

                //
                Integer numericScale = parameterNode.getIntAttribute("numericScale");

                //
                ParameterMode modeEnum = resolveParameterMode(mode);

                //
                Class<?> javaTypeClass = resolveClass(javaType);

                //
                JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

                //
                @SuppressWarnings("unchecked")
                Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);

                //
                ParameterMapping parameterMapping = builderAssistant.buildParameterMapping(parameterClass, property, javaTypeClass, jdbcTypeEnum, resultMap, modeEnum, typeHandlerClass, numericScale);

                //
                parameterMappings.add(parameterMapping);
            }

            //
            builderAssistant.addParameterMap(id, parameterClass, parameterMappings);
        }
    }

    /**
     * 解析resultMap
     *
     * @param list 所有的resultMap节点对象
     * @throws Exception 解析异常
     */
    private void resultMapElements(List<XNode> list) throws Exception {

        //遍历所有的resultMap节点
        for (XNode resultMapNode : list) {

            try {

                //解析resultMap
                resultMapElement(resultMapNode);
            } catch (IncompleteElementException e) {
                // ignore, it will be retried
            }
        }
    }

    /**
     * 解析具体的resultMapNode
     *
     * @param resultMapNode resultMap节点
     * @return 解析出来的ResultMap对象
     * @throws Exception 解析异常
     */
    private ResultMap resultMapElement(XNode resultMapNode) throws Exception {

        return resultMapElement(resultMapNode, Collections.<ResultMapping>emptyList());
    }

    /**
     * 解析具体的resultMapNode
     *
     * @param resultMapNode            resultMap节点
     * @param additionalResultMappings
     * @return 解析出来的ResultMap对象
     * @throws Exception 解析异常
     */
    private ResultMap resultMapElement(XNode resultMapNode, List<ResultMapping> additionalResultMappings) throws Exception {

        ErrorContext.instance().activity("processing " + resultMapNode.getValueBasedIdentifier());

        //id
        String id = resultMapNode.getStringAttribute("id",
                resultMapNode.getValueBasedIdentifier());

        //结果类型
        String type = resultMapNode.getStringAttribute("type",
                resultMapNode.getStringAttribute("ofType",
                        resultMapNode.getStringAttribute("resultType",
                                resultMapNode.getStringAttribute("javaType"))));
        //结果类型的类
        Class<?> typeClass = resolveClass(type);

        //继承自那个节点
        String extend = resultMapNode.getStringAttribute("extends");

        //是否开启autoMapping
        Boolean autoMapping = resultMapNode.getBooleanAttribute("autoMapping");

        //鉴别器
        Discriminator discriminator = null;

        //
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        resultMappings.addAll(additionalResultMappings);

        //获取所有的子节点对象
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {

            if ("constructor".equals(resultChild.getName())) {
                //constructor子对象

                processConstructorElement(resultChild, typeClass, resultMappings);
            } else if ("discriminator".equals(resultChild.getName())) {
                //discriminator子对象

                discriminator = processDiscriminatorElement(resultChild, typeClass, resultMappings);
            } else {
                //其他的全部都是resultMapping对象

                //
                List<ResultFlag> flags = new ArrayList<ResultFlag>();


                if ("id".equals(resultChild.getName())) {
                    //id对象

                    flags.add(ResultFlag.ID);
                }

                //
                resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
            }
        }

        //
        ResultMapResolver resultMapResolver =
                new ResultMapResolver(builderAssistant, id, typeClass, extend, discriminator, resultMappings, autoMapping);

        //
        try {

            //
            return resultMapResolver.resolve();
        } catch (IncompleteElementException e) {

            configuration.addIncompleteResultMap(resultMapResolver);

            throw e;
        }
    }

    /**
     * 处理constructor节点
     *
     * @param resultChild    节点对象
     * @param resultType     结果类型类
     * @param resultMappings resultMappings对象
     * @throws Exception
     */
    private void processConstructorElement(XNode resultChild, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {

        List<XNode> argChildren = resultChild.getChildren();

        //
        for (XNode argChild : argChildren) {

            List<ResultFlag> flags = new ArrayList<ResultFlag>();

            //
            flags.add(ResultFlag.CONSTRUCTOR);

            //如果为idArg
            if ("idArg".equals(argChild.getName())) {

                //
                flags.add(ResultFlag.ID);
            }

            //
            resultMappings.add(buildResultMappingFromContext(argChild, resultType, flags));
        }
    }

    /**
     * 解析鉴别器对象
     *
     * @param context
     * @param resultType
     * @param resultMappings
     * @return
     * @throws Exception
     */
    private Discriminator processDiscriminatorElement(XNode context, Class<?> resultType, List<ResultMapping> resultMappings) throws Exception {

        //属性列名
        String column = context.getStringAttribute("column");

        //java类型名
        String javaType = context.getStringAttribute("javaType");

        //jdbc类型名
        String jdbcType = context.getStringAttribute("jdbcType");

        //类型处理器
        String typeHandler = context.getStringAttribute("typeHandler");

        //java类型类
        Class<?> javaTypeClass = resolveClass(javaType);

        //类型处理器类
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);

        //jdbc类型类
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

        //
        Map<String, String> discriminatorMap = new HashMap<String, String>();

        //
        for (XNode caseChild : context.getChildren()) {

            //列值
            String value = caseChild.getStringAttribute("value");

            //映射结果
            String resultMap = caseChild.getStringAttribute("resultMap", processNestedResultMappings(caseChild, resultMappings));

            //
            discriminatorMap.put(value, resultMap);
        }

        //创建并返回鉴别器对象
        return builderAssistant.buildDiscriminator(resultType, column, javaTypeClass, jdbcTypeEnum, typeHandlerClass, discriminatorMap);
    }

    /**
     * 解析<sql></sql>标签
     *
     * @param list          sql标签对象
     * @throws Exception
     */
    private void sqlElement(List<XNode> list) throws Exception {

        if (configuration.getDatabaseId() != null) {

            sqlElement(list, configuration.getDatabaseId());
        }

        sqlElement(list, null);
    }

    /**
     *
     * @param list
     * @param requiredDatabaseId
     * @throws Exception
     */
    private void sqlElement(List<XNode> list, String requiredDatabaseId) throws Exception {
        for (XNode context : list) {
            String databaseId = context.getStringAttribute("databaseId");
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (databaseIdMatchesCurrent(id, databaseId, requiredDatabaseId)) {
                sqlFragments.put(id, context);
            }
        }
    }

    private boolean databaseIdMatchesCurrent(String id, String databaseId, String requiredDatabaseId) {
        if (requiredDatabaseId != null) {
            if (!requiredDatabaseId.equals(databaseId)) {
                return false;
            }
        } else {
            if (databaseId != null) {
                return false;
            }
            // skip this fragment if there is a previous one with a not null databaseId
            if (this.sqlFragments.containsKey(id)) {
                XNode context = this.sqlFragments.get(id);
                if (context.getStringAttribute("databaseId") != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param context    节点对象
     * @param resultType 结果类型
     * @param flags
     * @return
     * @throws Exception
     */
    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) throws Exception {

        String property;

        //如果是constructor对象，则读取其name属性作为property属性
        if (flags.contains(ResultFlag.CONSTRUCTOR)) {

            property = context.getStringAttribute("name");
        } else {

            property = context.getStringAttribute("property");
        }

        //字段列名
        String column = context.getStringAttribute("column");

        //java类型名
        String javaType = context.getStringAttribute("javaType");

        //jdbc类型名
        String jdbcType = context.getStringAttribute("jdbcType");

        //
        String nestedSelect = context.getStringAttribute("select");

        //
        String nestedResultMap = context.getStringAttribute("resultMap",
                processNestedResultMappings(context, Collections.<ResultMapping>emptyList()));

        //
        String notNullColumn = context.getStringAttribute("notNullColumn");

        //
        String columnPrefix = context.getStringAttribute("columnPrefix");

        //类型处理器名
        String typeHandler = context.getStringAttribute("typeHandler");

        //
        String resultSet = context.getStringAttribute("resultSet");

        //
        String foreignColumn = context.getStringAttribute("foreignColumn");

        //
        boolean lazy = "lazy".equals(context.getStringAttribute("fetchType", configuration.isLazyLoadingEnabled() ? "lazy" : "eager"));

        //
        Class<?> javaTypeClass = resolveClass(javaType);

        //
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler<?>> typeHandlerClass = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);

        //
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);

        //
        return builderAssistant.buildResultMapping(resultType, property, column, javaTypeClass, jdbcTypeEnum, nestedSelect, nestedResultMap, notNullColumn, columnPrefix, typeHandlerClass, flags, resultSet, foreignColumn, lazy);
    }

    private String processNestedResultMappings(XNode context, List<ResultMapping> resultMappings) throws Exception {
        if ("association".equals(context.getName())
                || "collection".equals(context.getName())
                || "case".equals(context.getName())) {
            if (context.getStringAttribute("select") == null) {
                ResultMap resultMap = resultMapElement(context, resultMappings);
                return resultMap.getId();
            }
        }
        return null;
    }

    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            if (boundType != null) {
                if (!configuration.hasMapper(boundType)) {
                    // Spring may not know the real resource name so we set a flag
                    // to prevent loading again this resource from the mapper interface
                    // look at MapperAnnotationBuilder#loadXmlResource
                    configuration.addLoadedResource("namespace:" + namespace);
                    configuration.addMapper(boundType);
                }
            }
        }
    }

}
