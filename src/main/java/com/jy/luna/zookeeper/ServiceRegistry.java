package com.jy.luna.zookeeper;

import com.jy.luna.commons.Stuff;
import com.jy.luna.xsd.LunaXsdHandler;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;


    public ServiceRegistry() {

        this.registryAddress = LunaXsdHandler.address;
    }

    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                AddRootNode(zk); // Add root node if not exist
                createNode(zk, data);
            }
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Stuff.ZK_SESSION_TIMEOUT, (WatchedEvent event) -> {
                    if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                        latch.countDown();
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
            Stat s = zk.exists(Stuff.ZK_REGISTRY_PATH, false);
            if (s == null) {
                zk.create(Stuff.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOGGER.debug("Luna: create zookeeper root node ({})", Stuff.ZK_REGISTRY_PATH);
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
    }

//    http://blog.csdn.net/lipeng_bigdata/article/details/50986845  ZooKeeper目录节点形式CreateMode
    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(Stuff.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);//// TODO: 2017/6/21
            LOGGER.debug("Luna: create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException ex){
            LOGGER.error("Luna: ", ex);
            ex.printStackTrace();
        }
    }

    /*public static void main(String[] args) throws InterruptedException {

        String address = "localhost:2181";

        ServiceRegistry sry = new ServiceRegistry();
        sry.register("nevermore");

        Thread.currentThread().sleep(99000);

        ServiceDiscovery sdc = new ServiceDiscovery(null);
        System.out.println(sdc.discover());
    }*/
}