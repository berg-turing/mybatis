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
import java.util.Properties;
import javax.sql.DataSource;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.AutoMappingUnknownColumnBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XMLConfigBuilder extends BaseBuilder {

    /**
     * 解析的状态
     */
    private boolean parsed;

    /**
     *
     *
     */
    private final XPathParser parser;


    /**
     * 使用的数据库环境的名
     */
    private String environment;


    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public XMLConfigBuilder(Reader reader) {

        this(reader, null, null);
    }

    public XMLConfigBuilder(Reader reader, String environment) {

        this(reader, environment, null);
    }

    public XMLConfigBuilder(Reader reader, String environment, Properties props) {

        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    public XMLConfigBuilder(InputStream inputStream) {

        this(inputStream, null, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment) {

        this(inputStream, environment, null);
    }

    public XMLConfigBuilder(InputStream inputStream, String environment, Properties props) {

        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), environment, props);
    }

    /**
     * 初始化XMLConfigBuilder对象
     *
     * @param parser
     * @param environment
     * @param props
     */
    private XMLConfigBuilder(XPathParser parser, String environment, Properties props) {

        super(new Configuration());

        //
        ErrorContext.instance().resource("SQL Mapper Configuration");


        this.configuration.setVariables(props);
        this.parsed = false;
        this.environment = environment;
        this.parser = parser;
    }

    /**
     * 获取配置的Configuration对象
     *
     * @return
     */
    public Configuration parse() {

        if (parsed) {
            throw new BuilderException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;

        //解析mybatisConfig.xml配置文件
        parseConfiguration(parser.evalNode("/configuration"));

        //解析完成，返回配置对象
        return configuration;
    }

    /**
     * 解析mybatisConfig.xml文件, properties, setting, typeAliases, plugins,
     * objectFactory, objectWrapperFactory, environments, databaseIdProvider
     * typeHandlers, mappers
     *
     * @param root
     */
    private void parseConfiguration(XNode root) {

        try {

            //issue #117 read properties first
            //解析properties(配置文件)标签内容
            propertiesElement(root.evalNode("properties"));

            //解析settings(属性设置)标签内容
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            //加载特殊VFS实现
            loadCustomVfs(settings);

            //注册类型别名
            typeAliasesElement(root.evalNode("typeAliases"));

            //解析plugins(插件)标签内容
            pluginElement(root.evalNode("plugins"));

            //解析对象工厂
            objectFactoryElement(root.evalNode("objectFactory"));


            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));


            reflectorFactoryElement(root.evalNode("reflectorFactory"));

            //配置所有的设置
            settingsElement(settings);


            // read it after objectFactory and objectWrapperFactory issue #631
            //解析environments(数据库环境)标签内容
            environmentsElement(root.evalNode("environments"));

            //解析数据库厂商标识
            databaseIdProviderElement(root.evalNode("databaseIdProvider"));

            //解析typeHandlers(类型处理)标签
            typeHandlerElement(root.evalNode("typeHandlers"));

            //解析mappers(mapper文件)标签
            mapperElement(root.evalNode("mappers"));

        } catch (Exception e) {


            throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    /**
     * 将settings标签中的所有属性配置项转换成一个Properties对象,并判断该配置项在系统中是否存在
     * 如果mybatis没有这个配置项,就抛出异常
     *
     * @param context
     * @return
     */
    private Properties settingsAsProperties(XNode context) {

        //如果节点没有内容,也就是节点为空,就返回一个空的配置对象
        if (context == null) {
            return new Properties();
        }

        //将settings所有子节点转化成一个Properties对象
        Properties props = context.getChildrenAsProperties();

        // Check that all settings are known to the configuration class
        MetaClass metaConfig = MetaClass.forClass(Configuration.class, localReflectorFactory);

        //遍历获取的所有的设置项
        for (Object key : props.keySet()) {

            //判断该设置项是否存在,如果不存在就抛出异常
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("The setting " + key + " is not known.  Make sure you spelled it correctly (case sensitive).");
            }
        }

        //通过判断,返回该配置对象
        return props;
    }

    /**
     * @param props
     * @throws ClassNotFoundException
     */
    private void loadCustomVfs(Properties props) throws ClassNotFoundException {

        String value = props.getProperty("vfsImpl");

        if (value != null) {
            String[] clazzes = value.split(",");

            for (String clazz : clazzes) {

                if (!clazz.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends VFS> vfsImpl = (Class<? extends VFS>) Resources.classForName(clazz);

                    configuration.setVfsImpl(vfsImpl);
                }
            }
        }
    }

    /**
     * 注册类型别名
     *
     * @param parent
     */
    private void typeAliasesElement(XNode parent) {

        //该节点不为空就执行
        if (parent != null) {

            //遍历所有的子标签
            for (XNode child : parent.getChildren()) {

                //如果子标签是package标签
                if ("package".equals(child.getName())) {

                    //获取包名
                    String typeAliasPackage = child.getStringAttribute("name");

                    //注册包名
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);

                } else {
                    //如果子标签是alias

                    //获取别名
                    String alias = child.getStringAttribute("alias");
                    //获取类型
                    String type = child.getStringAttribute("type");

                    try {

                        //获取class对象
                        Class<?> clazz = Resources.classForName(type);

                        //别名为空
                        if (alias == null) {

                            //使用默认的名字
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            //别名不为空, 就设置类型的别名

                            typeAliasRegistry.registerAlias(alias, clazz);
                        }

                    } catch (ClassNotFoundException e) {

                        //类型注册失败,一般是指定的类型不存在引起的错误,
                        //也就是Resources.classForName(type)执行出错
                        throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                    }
                }
            }
        }
    }

    /**
     * 解析插件的配置
     *
     * @param parent
     * @throws Exception
     */
    private void pluginElement(XNode parent) throws Exception {

        //如果有插件的配置
        if (parent != null) {

            //遍历插件配置下面的所有子标签, 也就是plugin标签
            for (XNode child : parent.getChildren()) {

                //获取plugin标签的interceptor属性
                String interceptor = child.getStringAttribute("interceptor");

                //将该plugin标签下面所有的property属性转换成Properties对象
                Properties properties = child.getChildrenAsProperties();

                //根据配置的interceptor字符串创建一个Interceptor对象
                Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).newInstance();

                //设置该对象的properties属性,其实就是该标签下所有的property属性
                interceptorInstance.setProperties(properties);

                //在配置对象中添加Interceptor对象
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    /**
     * 解析对象工厂
     *
     * @param context 对象工厂的配置节点
     * @throws Exception 解析异常错误
     */
    private void objectFactoryElement(XNode context) throws Exception {

        if (context != null) {
            //对象工厂类
            String type = context.getStringAttribute("type");

            //配置项
            Properties properties = context.getChildrenAsProperties();

            //创建对象工厂实例
            ObjectFactory factory = (ObjectFactory) resolveClass(type).newInstance();

            //设置其所有的配置项
            factory.setProperties(properties);

            //设置对象工厂
            configuration.setObjectFactory(factory);
        }
    }

    /**
     * @param context
     * @throws Exception
     */
    private void objectWrapperFactoryElement(XNode context) throws Exception {

        if (context != null) {
            //
            String type = context.getStringAttribute("type");

            //
            ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).newInstance();

            //
            configuration.setObjectWrapperFactory(factory);
        }
    }

    /**
     * @param context
     * @throws Exception
     */
    private void reflectorFactoryElement(XNode context) throws Exception {

        if (context != null) {
            //
            String type = context.getStringAttribute("type");

            //
            ReflectorFactory factory = (ReflectorFactory) resolveClass(type).newInstance();

            //
            configuration.setReflectorFactory(factory);
        }
    }

    /**
     * 解析properties标签中的内容
     *
     * @param context <properties></properties>节点对象
     * @throws Exception resource与url属性同时存在时，抛出异常
     */
    private void propertiesElement(XNode context) throws Exception {

        if (context != null) {

            //1.先获取<properties></properties>下面所有配置的参数
            Properties defaults = context.getChildrenAsProperties();

            //获取properties节点的resource属性
            String resource = context.getStringAttribute("resource");

            //获取properties节点的url属性
            String url = context.getStringAttribute("url");

            //这两个属性的值不能同时存在,否则就会报下面的错误
            if (resource != null && url != null) {

                throw new BuilderException(
                        "The properties element cannot specify both a URL and a resource based property file reference.  " +
                                "Please specify one or the other."
                );
            }

            //2.获取指定resource或者url中的配置，并合并已有的配置
            //如果指定的是resource属性
            if (resource != null) {

                //将resource字符串解析成Properties对象
                defaults.putAll(Resources.getResourceAsProperties(resource));

            } else if (url != null) {
                //如果指定的是url属性

                //将url字符串解析成Properties对象
                defaults.putAll(Resources.getUrlAsProperties(url));
            }

            //3.获取configuration对象中的所有配置项，并添加到配置对象中
            //将配置对象中的所有配置追加到当前的配置对象中
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }

            //重新设置配置
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    /**
     * 配置所有的设置
     * 如果配置文件中设置了该设置, 就应用配置了的值
     * 如果没有配置, 就使用默认的值
     *
     * @param props
     * @throws Exception
     */
    private void settingsElement(Properties props) throws Exception {

        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        configuration.setAutoMappingUnknownColumnBehavior(AutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
        configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        configuration.setDefaultExecutorType(ExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
        configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
        @SuppressWarnings("unchecked")
        Class<? extends TypeHandler> typeHandler = (Class<? extends TypeHandler>) resolveClass(props.getProperty("defaultEnumTypeHandler"));
        configuration.setDefaultEnumTypeHandler(typeHandler);
        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        configuration.setLogPrefix(props.getProperty("logPrefix"));
        @SuppressWarnings("unchecked")
        Class<? extends Log> logImpl = (Class<? extends Log>) resolveClass(props.getProperty("logImpl"));
        configuration.setLogImpl(logImpl);
        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    }

    /**
     * 解析environments标签内容
     *
     * @param context
     * @throws Exception
     */
    private void environmentsElement(XNode context) throws Exception {

        if (context != null) {

            //如果设置了默认的数据库环境属性, 就获取该属性的值
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }

            //获取所有的数据库环境的设置
            for (XNode child : context.getChildren()) {

                //获取数据库环境的id
                String id = child.getStringAttribute("id");

                //判断该字节点是否是使用的数据库环境配置
                if (isSpecifiedEnvironment(id)) {

                    //获取数据库事务工厂对象
                    TransactionFactory txFactory =
                            //解析事务标签
                            transactionManagerElement(child.evalNode("transactionManager"));

                    //获取数据源工厂对象
                    DataSourceFactory dsFactory =
                            //解析数据源工厂标签
                            dataSourceElement(child.evalNode("dataSource"));

                    //获取数据源对象
                    DataSource dataSource = dsFactory.getDataSource();

                    //获取数据库环境构建对象
                    Environment.Builder environmentBuilder = new Environment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);

                    //构建数据库环境,并设置到配置中
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    /**
     * @param context
     * @throws Exception
     */
    private void databaseIdProviderElement(XNode context) throws Exception {

        DatabaseIdProvider databaseIdProvider = null;
        if (context != null) {
            String type = context.getStringAttribute("type");
            // awful patch to keep backward compatibility
            if ("VENDOR".equals(type)) {
                type = "DB_VENDOR";
            }
            Properties properties = context.getChildrenAsProperties();
            databaseIdProvider = (DatabaseIdProvider) resolveClass(type).newInstance();
            databaseIdProvider.setProperties(properties);
        }

        Environment environment = configuration.getEnvironment();
        if (environment != null && databaseIdProvider != null) {
            String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
            configuration.setDatabaseId(databaseId);
        }
    }

    /**
     * 解析数据库事务工厂标签
     *
     * @param context
     * @return
     * @throws Exception
     */
    private TransactionFactory transactionManagerElement(XNode context) throws Exception {

        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            TransactionFactory factory = (TransactionFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a TransactionFactory.");
    }

    /**
     * 解析数据源工厂标签
     *
     * @param context
     * @return
     * @throws Exception
     */
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {

        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = (DataSourceFactory) resolveClass(type).newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment declaration requires a DataSourceFactory.");
    }

    /**
     * @param parent
     * @throws Exception
     */
    private void typeHandlerElement(XNode parent) throws Exception {

        //该节点不为空就继续
        if (parent != null) {

            //遍历所有的字节点, 即typeHandler或则package
            for (XNode child : parent.getChildren()) {

                //如果是package标签
                if ("package".equals(child.getName())) {

                    String typeHandlerPackage = child.getStringAttribute("name");
                    typeHandlerRegistry.register(typeHandlerPackage);

                } else {
                    //如果是typeHandler标签

                    String javaTypeName = child.getStringAttribute("javaType");
                    String jdbcTypeName = child.getStringAttribute("jdbcType");
                    String handlerTypeName = child.getStringAttribute("handler");

                    Class<?> javaTypeClass = resolveClass(javaTypeName);
                    JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                    Class<?> typeHandlerClass = resolveClass(handlerTypeName);

                    if (javaTypeClass != null) {
                        if (jdbcType == null) {

                            typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                        } else {

                            typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                        }
                    } else {

                        typeHandlerRegistry.register(typeHandlerClass);
                    }
                }
            }
        }
    }

    /**
     * 处理mappers标签内容
     *
     * @param parent
     * @throws Exception
     */
    private void mapperElement(XNode parent) throws Exception {

        if (parent != null) {

            for (XNode child : parent.getChildren()) {

                if ("package".equals(child.getName())) {

                    String mapperPackage = child.getStringAttribute("name");
                    configuration.addMappers(mapperPackage);

                } else {

                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");

                    if (resource != null && url == null && mapperClass == null) {
                        //resource类型的mapper

                        ErrorContext.instance().resource(resource);

                        //根据资源, 获取mapper的输入流
                        InputStream inputStream = Resources.getResourceAsStream(resource);

                        //mapper解析
                        XMLMapperBuilder mapperParser =
                                new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        mapperParser.parse();

                    } else if (resource == null && url != null && mapperClass == null) {
                        //url类型的mapper

                        ErrorContext.instance().resource(url);

                        //根据url, 获得mapper的输入流
                        InputStream inputStream = Resources.getUrlAsStream(url);

                        //mapper解析
                        XMLMapperBuilder mapperParser =
                                new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                        mapperParser.parse();

                    } else if (resource == null && url == null && mapperClass != null) {
                        //指定了mapper接口的mapper

                        Class<?> mapperInterface = Resources.classForName(mapperClass);

                        //将接口添加到mapper中
                        configuration.addMapper(mapperInterface);

                    } else {

                        throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                    }
                }
            }
        }
    }

    /**
     * 判断该数据库环境是否为当前使用的数据库环境
     *
     * @param id
     * @return
     */
    private boolean isSpecifiedEnvironment(String id) {

        if (environment == null) {

            //没有设置数据库环境, 抛出异常
            throw new BuilderException("No environment specified.");

        } else if (id == null) {

            //数据库环境没有设置id值
            throw new BuilderException("Environment requires an id attribute.");
        } else if (environment.equals(id)) {

            //找到数据库的环境
            return true;
        }

        return false;
    }

}
