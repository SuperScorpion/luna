package com.jy.luna.client.proxy;

import com.jy.luna.client.ClientHandler;
import com.jy.luna.client.ClientHandlerManager;
import com.jy.luna.client.RpcFuture;
import com.jy.luna.protocol.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by neo on 2017-06-25.
 */
public class ProxyHandler implements InvocationHandler {

    private Class<?> cls;

    public ProxyHandler(Class<?> c) {
        this.cls = c;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(proxy.getClass().getName() + String.valueOf(method) + "invoke exception!");
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(cls.getName());
//        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterClassTypes(method.getParameterTypes());
        request.setParameters(args);


        ClientHandler handler = ClientHandlerManager.getInstance().chooseHandler(cls.getName());
        RpcFuture rpcFuture = handler.channelWrite0(request);
        return rpcFuture.get();
    }
}
