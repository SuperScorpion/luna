package com.jy.luna.zookeeper;

import com.jy.luna.client.ClientCoreProcessor;
import com.jy.luna.client.ClientStuff;
import com.jy.luna.stuff.common.LunaConfigure;
import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.LunaXsdHandler;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private ClientStuff clientStuff;

    private ZooKeeper zookeeper;


    public ServiceDiscovery(ClientStuff clientStuff) {

        this.clientStuff = clientStuff;

        zookeeper = connectServer();

        if (zookeeper != null) {
            watchTopNode(zookeeper);
        } else {
            throw new LunaException("Luna: The zookeeper in discovery is null");
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(LunaXsdHandler.address, LunaConfigure.ZK_SESSION_TIMEOUT, (WatchedEvent event) -> {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    } else if(event.getState() == Watcher.Event.KeeperState.Expired) {
                        LOGGER.warn("Luna: The client zookeeper session is expired");
                    } else {
                        //do nothing
                    }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Luna: ", e);
            e.printStackTrace();
        }
        return zk;
    }


    private void watchTopNode(ZooKeeper zk) {
        try {
            List<String> serviceNodeList = zk.getChildren(LunaConfigure.ZK_REGISTRY_PATH, false);
            Set<String> servicePathList = LunaXsdHandler.serviceTimeoutMap.keySet();
            if(serviceNodeList != null && !serviceNodeList.isEmpty() && servicePathList != null && !servicePathList.isEmpty()) {
                for (String serviceNodeName : serviceNodeList) {
                    for (String se : servicePathList) {
                        if(LunaUtils.isNotBlank(serviceNodeName) && LunaUtils.isNotBlank(se) && se.equals(serviceNodeName)) {

                            LunaConfigure.execuService.submit(() -> {
                                    watchServiceNode(zk, serviceNodeName, LunaConfigure.ZK_REGISTRY_PATH + "/" + serviceNodeName);
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Luna: ", e);
            e.printStackTrace();
        }
    }

    private void watchServiceNode(ZooKeeper zk, String serviceFullName, String serviceNodePath) {
        try {
            List<String> ipNodeList = zk.getChildren(serviceNodePath, (WatchedEvent event) -> {
                    if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        LOGGER.debug("Luna: " + serviceFullName + " NodeChildrenChanged Now...");
                        watchServiceNode(zk, serviceFullName, serviceNodePath);//注册用一次失效
                    }
            });
            List<String> dataList = null;
            if(ipNodeList != null && !ipNodeList.isEmpty()) {
                dataList = new ArrayList<>();
                for (String ipNode : ipNodeList) {
                    byte[] ipByte = zk.getData(serviceNodePath + "/" + ipNode, false, null);
                    dataList.add(new String(ipByte));
                }
            }
//                LOGGER.debug("Luna: node data: {}", dataList);
//                this.dataList = dataList;

            LOGGER.debug("Luna: " + serviceFullName + " discovery try to add or update server node.");
            ClientCoreProcessor.getInstance().refreshLocalServerByThisService(serviceFullName, dataList, clientStuff);

        } catch (Exception e) {
            LOGGER.error("Luna: ", e);
            e.printStackTrace();
        }
    }

    public void stop(){
        if(zookeeper != null){
            try {
                zookeeper.close();
            } catch (InterruptedException e) {
                LOGGER.error("Luna: ", e);
                e.printStackTrace();
            }
        }
    }
}
