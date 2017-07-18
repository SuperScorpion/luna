package com.jy.luna.client.proxy;

import java.lang.reflect.Proxy;

/**
 * Created by neo on 2017/6/25.
 */
public class ProxyFactory {

    public static Object createProxy(Class<?> objClass) throws Exception {

        //创建InvocationHandler
        ProxyHandler handler = new ProxyHandler(objClass);

        //返回代理
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{objClass}, handler);
    }
}
