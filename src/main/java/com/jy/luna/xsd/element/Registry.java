package com.jy.luna.xsd.element;

import com.jy.luna.stuff.LunaSpringReform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by neo on 2017/6/27.
 * C:
 * registrAddress
 * timeout
 * S:
 * registrAddress
 * port
 */
public class Registry implements ApplicationContextAware {

    private String id;

    private String address;

    private String roundRobin;


    public String getRoundRobin() {
        return roundRobin;
    }

    public void setRoundRobin(String roundRobin) {
        this.roundRobin = roundRobin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LunaSpringReform.reform(applicationContext);
    }
}
