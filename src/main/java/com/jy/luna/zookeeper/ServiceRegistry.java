package com.jy.luna.zookeeper;

import com.jy.luna.stuff.common.LunaConfigure;
import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.LunaXsdHandler;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);


    public ServiceRegistry() {

    }

    public void register(Map<String, Object> serviceBeanMap, String data) {
        if (data != null && LunaUtils.isNotBlank(data)) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                AddRootNode(zk); // Add root node if not exist
                createServiceNode(zk, serviceBeanMap, data);
            } else {
                throw new LunaException("Luna: The zookeeper in discovery is null");
            }
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(LunaXsdHandler.address, LunaConfigure.ZK_SESSION_TIMEOUT, (WatchedEvent event) -> {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    } else if(event.getState() == Watcher.Event.KeeperState.Expired) {
                        LOGGER.warn("Luna: The server zookeeper session is expired");
                    } else {
                        //do nothing
                    }
            });
            latch.await();
        } catch (IOException | InterruptedException ex){
            LOGGER.error("Luna: ", ex);
            ex.printStackTrace();
        }
        return zk;
    }

    private void AddRootNode(ZooKeeper zk){
        try {
            Stat s = zk.exists(LunaConfigure.ZK_REGISTRY_PATH, false);
            if (s == null) {
                zk.create(LunaConfigure.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOGGER.debug("Luna: create zookeeper root node ({})", LunaConfigure.ZK_REGISTRY_PATH);
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
    }

    private void createServiceNode(ZooKeeper zk, Map<String, Object> serviceBeanMap, String data) {
        if(serviceBeanMap != null && !serviceBeanMap.isEmpty()) {
            Set<String> servicePathSet = serviceBeanMap.keySet();

            for (String servicePath : servicePathSet) {
                String serPath = LunaConfigure.ZK_REGISTRY_PATH + "/" + servicePath;
                try {
                    Stat s = zk.exists(serPath, false);
                    if (s == null) {
                        zk.create(serPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);//// TODO: 2017/6/21
                    }
                    createIpNode(zk, data, serPath);//创建地址节点 并赋予data

                } catch (KeeperException | InterruptedException ex) {
                    LOGGER.error("Luna: ", ex);
                    ex.printStackTrace();
                }
            }
        }
    }

//    http://blog.csdn.net/lipeng_bigdata/article/details/50986845  ZooKeeper目录节点形式CreateMode
    private void createIpNode(ZooKeeper zk, String data, String serPath) {
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(serPath + "/" + data, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//// TODO: 2017/6/21
            LOGGER.debug("Luna: create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException ex){
            LOGGER.error("Luna: ", ex);
            ex.printStackTrace();
        }
    }

}