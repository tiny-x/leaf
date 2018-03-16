package com.leaf.spring.schema;

import com.google.common.base.Strings;
import com.leaf.spring.init.bean.SpringConsumer;
import com.leaf.spring.init.bean.SpringProvider;
import com.leaf.spring.init.bean.SpringReferenceBean;
import com.leaf.spring.init.bean.SpringServiceBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LeafBeanDefinitionParser implements BeanDefinitionParser {

    private Class<?> aClass;

    public LeafBeanDefinitionParser(Class<?> aClass) {
        this.aClass = aClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        if (aClass == SpringProvider.class) {
            return parseProvider(element, parserContext);
        } else if (aClass == SpringServiceBean.class) {
            return parseServiceBean(element, parserContext);
        } else if (aClass == SpringConsumer.class) {
            return parseConsumer(element, parserContext);
        } else if (aClass == SpringReferenceBean.class) {
            return parseReferenceBean(element, parserContext);
        } else {
            throw new BeanDefinitionValidationException("Unknown class to definition: " + aClass.getName());
        }
    }

    private BeanDefinition parseReferenceBean(Element element, ParserContext parserContext) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(aClass);

        addPropertyReference(definition, element, "consumer", true);
        addProperty(definition, element, "interfaceClass", true);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(definition, (Element) item, "group", false);
                    addProperty(definition, (Element) item, "serviceProviderName", false);
                    addProperty(definition, (Element) item, "version", false);
                    addProperty(definition, element, "dispatchType", false);
                    addProperty(definition, element, "serializerType", false);
                    addProperty(definition, element, "loadBalancerType", false);
                    addProperty(definition, element, "strategy", false);
                    addProperty(definition, element, "retries", false);
                    addProperty(definition, element, "invokeType", false);
                }
            }
        }

        return registerBean(definition, element, parserContext);
    }

    private BeanDefinition parseConsumer(Element element, ParserContext parserContext) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(aClass);

        addProperty(definition, element, "id", true);
        addProperty(definition, element, "registerType", true);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(definition, (Element) item, "timeout", false);
                    addProperty(definition, (Element) item, "registryServer", false);
                }
            }
        }
        return registerBean(definition, element, parserContext);
    }

    private BeanDefinition parseServiceBean(Element element, ParserContext parserContext) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(aClass);

        addProperty(definition, element, "interfaceClass", false);
        addPropertyReference(definition, element, "provider", true);
        addPropertyReference(definition, element, "ref", true);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(definition, (Element) item, "weight", false);
                    addProperty(definition, (Element) item, "group", false);
                    addProperty(definition, (Element) item, "serviceProviderName", false);
                    addProperty(definition, (Element) item, "version", false);
                }
            }
        }
        return registerBean(definition, element, parserContext);
    }

    private BeanDefinition parseProvider(Element element, ParserContext parserContext) {
        GenericBeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClass(aClass);

        addProperty(definition, element, "registerType", true);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                String localName = item.getLocalName();
                if ("property".equals(localName)) {
                    addProperty(definition, (Element) item, "port", false);
                    addProperty(definition, (Element) item, "registryServer", false);
                }
            }
        }
        return registerBean(definition, element, parserContext);
    }

    private BeanDefinition registerBean(GenericBeanDefinition definition, Element element, ParserContext parserContext) {
        String id = element.getAttribute("id");
        if (Strings.isNullOrEmpty(id)) {
            id = aClass.getSimpleName();
        }
        if (parserContext.getRegistry().containsBeanDefinition(id)) {
            throw new IllegalStateException("Duplicate bean id: " + id);
        }
        BeanDefinitionHolder holder = new BeanDefinitionHolder(definition, id);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());

        return definition;
    }

    private static void addProperty(GenericBeanDefinition definition, Element element, String propertyName, boolean required) {
        String ref = element.getAttribute(propertyName);
        if (required) {
            checkAttribute(propertyName, ref);
        }
        if (!Strings.isNullOrEmpty(ref)) {
            definition.getPropertyValues().addPropertyValue(propertyName, ref);
        }
    }

    private static void addPropertyReference(GenericBeanDefinition definition, Element element, String propertyName, boolean required) {
        String ref = element.getAttribute(propertyName);
        if (required) {
            checkAttribute(propertyName, ref);
        }
        if (!Strings.isNullOrEmpty(ref)) {
            definition.getPropertyValues().addPropertyValue(propertyName, new RuntimeBeanReference(ref));
        }
    }

    private static void addPropertyObject(GenericBeanDefinition definition, Element element, String propertyName, boolean required) {
        String ref = element.getAttribute(propertyName);
        if (required) {
            checkAttribute(propertyName, ref);
        }
        if (!Strings.isNullOrEmpty(ref)) {
            Object object;
            try {
                object = Class.forName(ref).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            definition.getPropertyValues().addPropertyValue(propertyName, object);
        }
    }

    private static String checkAttribute(String attributeName, String attribute) {
        if (Strings.isNullOrEmpty(attribute)) {
            throw new BeanDefinitionValidationException("attribute [" + attributeName + "] is required.");
        }
        return attribute;
    }
}
