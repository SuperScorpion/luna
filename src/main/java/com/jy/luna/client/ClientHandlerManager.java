package com.jy.luna.client;

import com.jy.luna.xsd.LunaXsdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by neo on 2017/6/24.
 */
public class ClientHandlerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandlerManager.class);

    private volatile static ClientHandlerManager handlerManager;

    private Map<InetSocketAddress, ClientHandler> connectedServerNodesMap = new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private CopyOnWriteArrayList<ClientHandler> handlerList = new CopyOnWriteArrayList<>();
    private AtomicInteger roundRobinFlag = new AtomicInteger(0);

    private long chooseLoopWaitTime = 6000;

    private ClientHandlerManager() {
    }

    public static ClientHandlerManager getInstance() {
        if (handlerManager == null) {
//            synchronized (ClientHandlerManager.class) {
                handlerManager = new ClientHandlerManager();
//            }
        }
        return handlerManager;
    }


    public void addHandler(ClientHandler handler) {
        handlerList.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodesMap.put(remoteAddress, handler);
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
        int size = handlerList.size();
        while (size == 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    size = handlerList.size();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Luna: Waiting for available node is interrupted! ", e);
                e.printStackTrace();
            }
        }

        int index;
        if(LunaXsdHandler.isRoundRobin) {
            index = (roundRobinFlag.getAndAdd(1) + size) % size;
        } else {
            index = ThreadLocalRandom.current().nextInt(size);
        }

        return handlerList.get(index);
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return condition.await(chooseLoopWaitTime, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void addOrUpdateLocalServerInfo(List<String> addressList ,ClientStuff clientStuff) throws Exception {

        if(addressList != null && !addressList.isEmpty()) {

            List<InetSocketAddress> isaList =  convertAddressList(addressList);

            if (connectedServerNodesMap.isEmpty()) {//first
                //add
                for (InetSocketAddress isa : isaList) {
                    clientStuff.connectServerProcessor(isa);
                }
            } else {

                //add new and delete the abandoned
                Set<InetSocketAddress> oriIsaSet = new HashSet(connectedServerNodesMap.keySet());
                Set<InetSocketAddress> mixIsaSet = new HashSet<>();//遗留的可用的

                for(InetSocketAddress isa : isaList) {
                    if(!oriIsaSet.contains(isa)) {//new add
                        clientStuff.connectServerProcessor(isa);
                    } else {
                        mixIsaSet.add(isa);
                    }
                }

                //delete
                for(InetSocketAddress isa : oriIsaSet) {
                    if(!mixIsaSet.contains(isa)) {
                        connectedServerNodesMap.remove(isa);
                    }
                }

                //help gc
                oriIsaSet.clear();
                mixIsaSet.clear();

            }
        }
    }

    private List<InetSocketAddress> convertAddressList(List<String> addressList) {

        List<InetSocketAddress> resultList = new ArrayList<>();

        if (addressList != null && !addressList.isEmpty()) {

            for (String address : addressList) {

                String[] addArra = address.split(":");

                if (addArra.length != 2) continue;

                InetSocketAddress isa = new InetSocketAddress(addArra[0], Integer.parseInt(addArra[1]));

                resultList.add(isa);
            }
        }

        return resultList;
    }
}
