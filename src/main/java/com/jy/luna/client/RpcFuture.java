package com.jy.luna.client;

import com.jy.luna.protocol.RpcRequest;
import com.jy.luna.protocol.RpcResponse;
import com.jy.luna.stuff.common.LunaConfigure;
import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RpcFuture implements Future<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcFuture.class);

    private RpcRequest request;
    private RpcResponse response;
    private long startTime;

    private long responseTimeThreshold;//timeout

    private boolean isDone = false;

    private ReentrantLock lock = new ReentrantLock();
    private Condition lockCondi = lock.newCondition();

    public RpcFuture(RpcRequest request, String timeout) {
        this.request = request;
        startTime = System.currentTimeMillis();
        this.responseTimeThreshold = LunaUtils.isBlank(timeout) ? 6000 : Long.valueOf(timeout);
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        lock.lock();//必须获得资源锁 await才起作用的呢呀
        try {
            if(!isDone()) lockCondi.await();//没返回之前一直阻塞调用线程
            if (response != null) {
                if(response.isError()) return LunaConfigure.FUTURE_ERROR_MSG;///有错误则返回
                return response.getResult();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        lock.lock();
        try {
            boolean success = lockCondi.await(timeout, unit);
            if(success) {
                if (response != null) {
                    return response.getResult();
                } else {
                    return null;
                }
            } else {
                throw new LunaException("Timeout exception. Request id: " + request.getRequestId()
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



    public void setResponse0(RpcResponse response) {
        lock.lock();
        try {
            this.isDone = true;
            this.response = response;
            if(lock.getWaitQueueLength(lockCondi) > 0) lockCondi.signalAll();//唤醒所有等待结果的线程

            long responseTime = System.currentTimeMillis() - startTime;
            if (responseTime > responseTimeThreshold)
                LOGGER.warn("Luna: Server response time is too slow. Request id = " + response.getRequestId() + ". Response Time = " + responseTime + "ms" + ". The responseTimeThreshold = " + responseTimeThreshold);
        } finally {
            lock.unlock();
        }
    }
}
