package com.jy.luna.xsd.element;

/**
 * Created by neo on 2017/6/27.
 */
public class Cli {

    private String id;

    private String service;

    private String timeout;


    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
