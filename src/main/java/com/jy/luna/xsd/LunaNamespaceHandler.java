package com.jy.luna.xsd;

/**
 * Created by neo on 2017/6/27.
 */

import com.jy.luna.xsd.element.Cli;
import com.jy.luna.xsd.element.Registry;
import com.jy.luna.xsd.element.Sev;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


public class LunaNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("sev", new MoonParser(Sev.class));
        registerBeanDefinitionParser("cli", new MoonParser(Cli.class));
        registerBeanDefinitionParser("registry", new MoonParser(Registry.class));
    }
}