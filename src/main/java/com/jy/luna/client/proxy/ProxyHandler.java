package com.jy.luna.client.proxy;

import com.jy.luna.client.ClientCoreProcessor;
import com.jy.luna.client.ClientHandler;
import com.jy.luna.client.RpcFuture;
import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.stuff.common.LunaConfigure;
import com.jy.luna.stuff.exception.LunaException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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


        int i = 1;
        return recurseInvoker(request, i);//实现调用失败自动重新选择handler再调
    }


    private Object recurseInvoker(RpcRequest request, int i) throws ExecutionException, InterruptedException {

        ClientHandler handler = ClientCoreProcessor.getInstance().chooseHandler(cls.getName());
        RpcFuture rpcFuture = handler.channelWrite0(request);

        //do other things by future mode

        Object result = rpcFuture.get(rpcFuture.getTimeOut(), TimeUnit.MILLISECONDS);//time out process

        if(LunaConfigure.FUTURE_ERROR_MSG.equals(result)) {
            if(i < LunaConfigure.FUTURE_ERROR_TIMES) {
                i += 1;
                recurseInvoker(request, i);
            } else {
                throw new LunaException("Luna: "+ request.getClassName() + "(" + request.getMethodName() + ") it has failed "+ i + " times to get result, Luna has shutdown it now.");
            }
        }

        return result;
    }
}
