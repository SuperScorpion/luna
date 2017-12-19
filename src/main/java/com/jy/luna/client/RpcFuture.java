package com.jy.luna.client;

import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.protocol.RpcResponse;
import com.jy.luna.stuff.common.LunaConfigure;
import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.LunaXsdHandler;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RpcFuture implements Future<Object> {

//    private static final Logger LOGGER = LoggerFactory.getLogger(RpcFuture.class);

    private RpcRequest request;
    private RpcResponse response;
//    private long startTime;

    private long timeout;//timeout

    private boolean isDone = false;

    private ReentrantLock lock = new ReentrantLock();
    private Condition lockCondition = lock.newCondition();

    public RpcFuture(RpcRequest request) {

        //设置service请求超时时间由用户决定的
        Map<String, String> stMap = LunaXsdHandler.serviceTimeoutMap;
        String timeout = null;
        if(stMap != null && !stMap.isEmpty()) {
            timeout =  LunaXsdHandler.serviceTimeoutMap.get(request.getClassName());
        }

        this.request = request;
//        startTime = System.currentTimeMillis();
        this.timeout = LunaUtils.isBlank(timeout) ? 6000 : Long.valueOf(timeout);
    }

    public Long getTimeOut() {
        return this.timeout;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public Object get() throws InterruptedException {
        lock.lock();//必须获得资源锁 await才起作用的呢呀
        try {
            if(!isDone()) lockCondition.await();//没返回之前一直阻塞调用线程
            if (response != null) {
                if(response.isError()) {
                    return LunaConfigure.FUTURE_ERROR_MSG;///有错误则返回
                } else {
                    return response.getResult();
                }
            } else {
                throw new LunaException("Luna: The future response is null");
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            boolean success = lockCondition.await(timeout, unit);
            if(success) {
                if (response != null) {
                    if(response.isError()) {
                        return LunaConfigure.FUTURE_ERROR_MSG;///有错误则返回
                    } else {
                        return response.getResult();
                    }
                } else {
                    throw new LunaException("Luna: The future response is null");
                }
                /*if (response != null) {
                    return response.getResult();
                } else {
                    return null;
                }*/
            } else {
                throw new LunaException("Luna: Timeout exception. Request id: " + request.getRequestId()
                        + ". Request class name: " + request.getClassName()
                        + ". Request method: " + request.getMethodName());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }


    /**
     * 回写返回的结果 并有超时监测
     * @param response
     */
    public void setResponse0(RpcResponse response) {
        lock.lock();
        try {
            this.isDone = true;
            this.response = response;
            if(lock.getWaitQueueLength(lockCondition) > 0) lockCondition.signalAll();//唤醒所有等待结果的线程

            /*long responseTime = System.currentTimeMillis() - startTime;
            if (responseTime > timeout) {
                LOGGER.warn("Luna: Server response time is too slow. Request id = "
                        + response.getRequestId() + ". Response Time = "
                        + responseTime + "ms" + ". The timeout = " + timeout);
            }*/
        } finally {
            lock.unlock();
        }
    }
}
