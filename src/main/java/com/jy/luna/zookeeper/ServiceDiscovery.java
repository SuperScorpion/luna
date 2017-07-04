package com.jy.luna.zookeeper;

import com.jy.luna.client.ClientHandlerManager;
import com.jy.luna.client.ClientStuff;
import com.jy.luna.stuff.LunaConfigure;
import com.jy.luna.xsd.LunaXsdHandler;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

//    private volatile List<String> dataList = new ArrayList<>();

    private ClientStuff clientStuff;

    private String registryAddress;

    private ZooKeeper zookeeper;

    public ServiceDiscovery(ClientStuff clientStuff) {

        this.registryAddress = LunaXsdHandler.address;
        this.clientStuff = clientStuff;

        zookeeper = connectServer();
        if (zookeeper != null) {
            watchNode(zookeeper);
        }
    }

    /*public String discover() {
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.debug("Luna: using only one data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("Luna: using random data: {}", data);
            }
        }

        *//*try {//模拟session保持
            Thread.sleep(70000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*//*
        return data;
    }*/

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, LunaConfigure.ZK_SESSION_TIMEOUT, (WatchedEvent event) -> {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Luna: ", e);
            e.printStackTrace();
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(LunaConfigure.ZK_REGISTRY_PATH, (WatchedEvent event) -> {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) watchNode(zk);
            });
            if(nodeList != null && !nodeList.isEmpty()) {
                List<String> dataList = new ArrayList<>();
                for (String node : nodeList) {
                    byte[] bytes = zk.getData(LunaConfigure.ZK_REGISTRY_PATH + "/" + node, false, null);
                    dataList.add(new String(bytes));
                }
                LOGGER.debug("Luna: node data: {}", dataList);
//                this.dataList = dataList;

                LOGGER.debug("Luna: Service discovery try to add or update connected server node.");
                ClientHandlerManager.getInstance().addOrUpdateLocalServerInfo(dataList, clientStuff);
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("Luna: ", e);
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.error("Luna: ", e);
            e.printStackTrace();
        }
    }

    public void stop(){
        if(zookeeper!=null){
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                LOGGER.error("Luna: ", e);
                e.printStackTrace();
            }
        }
    }
}
