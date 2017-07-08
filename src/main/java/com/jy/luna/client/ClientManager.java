package com.jy.luna.client;

/**
 * Created by neo on 2017/7/8.
 */

import com.jy.luna.xsd.LunaXsdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCoreProcessor.class);

    private List<ClientHandler> serviceHandlerList = new CopyOnWriteArrayList<>();

    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private AtomicInteger atoInt = new AtomicInteger(0);

    private long chooseLoopWaitTime = 6000;


    public List<ClientHandler> getServiceHandlerList() {
        return serviceHandlerList;
    }

    public void setServiceHandlerList(List<ClientHandler> serviceHandlerList) {
        this.serviceHandlerList = serviceHandlerList;
    }


    public void addHandler(ClientHandler handler) {

        serviceHandlerList.add(handler);

        signalAvailableHandler();
    }


    private void signalAvailableHandler() {
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }


    public ClientHandler chooseHandler() {

        int size = serviceHandlerList.size();
        while (size == 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    size = serviceHandlerList.size();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Luna: Waiting for available node is interrupted! ", e);
                e.printStackTrace();
            }
        }

        int index;
        if(LunaXsdHandler.isRoundRobin) {
            index = (atoInt.getAndAdd(1) + size) % size;
        } else {
            index = ThreadLocalRandom.current().nextInt(size);
        }

        return serviceHandlerList.get(index);
    }


    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return condition.await(chooseLoopWaitTime, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }
}
