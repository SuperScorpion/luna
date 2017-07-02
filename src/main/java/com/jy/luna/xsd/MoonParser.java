package com.jy.luna.xsd;

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * Created by neo on 2017/6/27.
 */
public class MoonParser extends AbstractSimpleBeanDefinitionParser {

    private Class<?> beanClass;

    public MoonParser(Class<?> bc) {
        this.beanClass = bc;
    }
    @Override
    protected Class<?> getBeanClass(Element element) {
        return this.beanClass;
    }
}
