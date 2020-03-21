package com.leaf.spring.schema;

import com.leaf.spring.init.bean.ConsumerFactory;
import com.leaf.spring.init.bean.ProviderFactoryBean;
import com.leaf.spring.init.bean.ReferenceFactoryBean;
import com.leaf.spring.init.bean.ServiceFactoryBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class LeafNameSpaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("provider", new LeafBeanDefinitionParser(ProviderFactoryBean.class));
        registerBeanDefinitionParser("consumer", new LeafBeanDefinitionParser(ConsumerFactory.class));
        registerBeanDefinitionParser("service", new LeafBeanDefinitionParser(ServiceFactoryBean.class));
        registerBeanDefinitionParser("reference", new LeafBeanDefinitionParser(ReferenceFactoryBean.class));
    }
}
