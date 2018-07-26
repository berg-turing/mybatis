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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class XMLIncludeTransformer {

    /**
     * 配置对象
     */
    private final Configuration configuration;

    /**
     *
     */
    private final MapperBuilderAssistant builderAssistant;

    /**
     * 构造
     *
     * @param configuration    配置对象
     * @param builderAssistant
     */
    public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    /**
     * 应用include
     *
     * @param source
     */
    public void applyIncludes(Node source) {
        //
        Properties variablesContext = new Properties();

        //获取配置值
        Properties configurationVariables = configuration.getVariables();

        //将configuration中的所有配置属性放在该配置属性中
        if (configurationVariables != null) {
            variablesContext.putAll(configurationVariables);
        }

        //处理include
        applyIncludes(source, variablesContext, false);
    }

    /**
     * Recursively apply includes through all SQL fragments.
     *
     * @param source           Include node in DOM tree
     * @param variablesContext Current context for static variables with values
     */
    private void applyIncludes(Node source, final Properties variablesContext, boolean included) {

        if (source.getNodeName().equals("include")) {
            //如果该节点是include节点，就执行

            Node toInclude = findSqlFragment(getStringAttribute(source, "refid"), variablesContext);

            Properties toIncludeContext = getVariablesContext(source, variablesContext);

            applyIncludes(toInclude, toIncludeContext, true);

            if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
                toInclude = source.getOwnerDocument().importNode(toInclude, true);
            }

            source.getParentNode().replaceChild(toInclude, source);
            while (toInclude.hasChildNodes()) {
                toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
            }

            toInclude.getParentNode().removeChild(toInclude);
        } else if (source.getNodeType() == Node.ELEMENT_NODE) {
            //如果该节点的类型为元素

            NodeList children = source.getChildNodes();

            //迭代执行
            for (int i = 0; i < children.getLength(); i++) {
                applyIncludes(children.item(i), variablesContext, included);
            }
        } else if (included && source.getNodeType() == Node.TEXT_NODE
                && !variablesContext.isEmpty()) {
            //如果节点类型为文档

            // replace variables ins all text nodes
            source.setNodeValue(PropertyParser.parse(source.getNodeValue(), variablesContext));
        }
    }

    /**
     * 寻找sql语句对象
     *
     * @param refid     id
     * @param variables 参数
     * @return
     */
    private Node findSqlFragment(String refid, Properties variables) {

        //
        refid = PropertyParser.parse(refid, variables);

        //
        refid = builderAssistant.applyCurrentNamespace(refid, true);

        try {
            XNode nodeToInclude = configuration.getSqlFragments().get(refid);

            return nodeToInclude.getNode().cloneNode(true);
        } catch (IllegalArgumentException e) {
            throw new IncompleteElementException("Could not find SQL statement to include with refid '" + refid + "'", e);
        }
    }

    /**
     * @param node
     * @param name
     * @return
     */
    private String getStringAttribute(Node node, String name) {

        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    /**
     * Read placeholders and their values from include node definition.
     *
     * @param node                      Include node instance
     * @param inheritedVariablesContext Current context used for replace variables in new variables values
     * @return variables context from include instance (no inherited values)
     */
    private Properties getVariablesContext(Node node, Properties inheritedVariablesContext) {

        Map<String, String> declaredProperties = null;
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String name = getStringAttribute(n, "name");
                // Replace variables inside
                String value = PropertyParser.parse(getStringAttribute(n, "value"), inheritedVariablesContext);
                if (declaredProperties == null) {
                    declaredProperties = new HashMap<String, String>();
                }
                if (declaredProperties.put(name, value) != null) {
                    throw new BuilderException("Variable " + name + " defined twice in the same include definition");
                }
            }
        }

        if (declaredProperties == null) {
            return inheritedVariablesContext;
        } else {
            Properties newProperties = new Properties();
            newProperties.putAll(inheritedVariablesContext);
            newProperties.putAll(declaredProperties);
            return newProperties;
        }
    }
}
