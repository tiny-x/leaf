package com.leaf.spring.schema;

import com.leaf.spring.init.bean.SpringConsumer;
import com.leaf.spring.init.bean.SpringProvider;
import com.leaf.spring.init.bean.SpringReferenceBean;
import com.leaf.spring.init.bean.SpringServiceBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class LeafNameSpaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("provider", new LeafBeanDefinitionParser(SpringProvider.class));
        registerBeanDefinitionParser("consumer", new LeafBeanDefinitionParser(SpringConsumer.class));
        registerBeanDefinitionParser("service", new LeafBeanDefinitionParser(SpringServiceBean.class));
        registerBeanDefinitionParser("reference", new LeafBeanDefinitionParser(SpringReferenceBean.class));
    }
}
