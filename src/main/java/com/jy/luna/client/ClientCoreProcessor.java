package com.jy.luna.client;

import com.jy.luna.stuff.common.LunaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * core client pro
 * Created by neo on 2017/6/24.
 */
public class ClientCoreProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCoreProcessor.class);

    private volatile static ClientCoreProcessor handlerManager;

    private Map<String, ClientManager> clientManagerMap = new ConcurrentHashMap<>();
    private Map<InetSocketAddress, ClientHandler> connectedClientHandlerMap = new ConcurrentHashMap<>();///////所有的handler总的记录

    private ClientCoreProcessor() {
    }

    ///高并发单例模式
    public static ClientCoreProcessor getInstance() {
        if (handlerManager == null) {
            synchronized (ClientCoreProcessor.class) {
                if(handlerManager == null) handlerManager = new ClientCoreProcessor();
            }
        }
        return handlerManager;
    }



    public void addHandler(ClientHandler handler, String serviceFullName) {

        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();

        connectedClientHandlerMap.put(remoteAddress, handler);

        clientManagerMap.get(serviceFullName).addHandler(handler);
    }


    public ClientHandler chooseHandler(String serviceFullName) {

        ClientManager cmr = clientManagerMap.get(serviceFullName);

        return cmr.chooseHandler();
    }



    /**
     * 对应的service添加handlerList
     * 有多个service的discovery会来竞争资源
     * @param clientStuff
     * @param isaList
     * @param serviceFullName
     * @throws Exception
     */
    private synchronized void currentServiceHandlerListAppender(ClientStuff clientStuff, List<InetSocketAddress> isaList, String serviceFullName) throws Exception {
        for (InetSocketAddress isa : isaList) {
            if(!connectedClientHandlerMap.containsKey(isa)) {
                clientStuff.connectServerProcessor(isa, serviceFullName);////不存在则连接产生handler并添加此handler asynchronous
            }  else {
                clientManagerMap.get(serviceFullName).addHandler(connectedClientHandlerMap.get(isa));
            }
        }
    }



    /**
     * 刷新当前的service对应信息
     * @param serviceFullName
     * @param freshAddressList
     * @param clientStuff
     * @throws Exception
     */
    public void refreshLocalServerByThisService(String serviceFullName, List<String> freshAddressList, ClientStuff clientStuff) throws Exception {

        if(freshAddressList != null && !freshAddressList.isEmpty()) {

            List<InetSocketAddress> isaList = convertAddressList(freshAddressList);

            if(!clientManagerMap.containsKey(serviceFullName)) {//add

                ClientManager cmr = new ClientManager();

                List<ClientHandler> currentServiceHandlerList = new CopyOnWriteArrayList<>();

                cmr.setServiceHandlerList(currentServiceHandlerList);

                clientManagerMap.put(serviceFullName, cmr);

                currentServiceHandlerListAppender(clientStuff, isaList, serviceFullName);///add

            } else {///update

                ClientManager cmr = clientManagerMap.get(serviceFullName);

                List<ClientHandler> currentServiceHandlerList = cmr.getServiceHandlerList();

                currentServiceHandlerList.clear();//delete

                deleteAbandonConnectedClientHandler(freshAddressList);///just delete abandoned

                currentServiceHandlerListAppender(clientStuff, isaList, serviceFullName);//add
            }
        } else {//delete to zero address or the initial is zero

            ClientManager cmr = clientManagerMap.get(serviceFullName);

            if(cmr != null && cmr.getServiceHandlerList() != null) cmr.getServiceHandlerList().clear();//delete
        }
    }


    public synchronized void deleteAbandonConnectedClientHandler(List<String> freshAddressList) throws Exception {

        if(freshAddressList != null && !freshAddressList.isEmpty()) {

            List<InetSocketAddress> isaList =  convertAddressList(freshAddressList);

            //add new and delete the abandoned
            Set<InetSocketAddress> oriIsaSet = new HashSet(connectedClientHandlerMap.keySet());

            //delete
            for(InetSocketAddress isa : oriIsaSet) {
                if(!isaList.contains(isa)) {
                    connectedClientHandlerMap.remove(isa);
                }
            }

            //help gc
            oriIsaSet.clear();
            oriIsaSet = null;
        }
    }

    private List<InetSocketAddress> convertAddressList(List<String> addressList) {

        List<InetSocketAddress> resultList = new ArrayList<>();

        if (addressList != null && !addressList.isEmpty()) {

            for (String address : addressList) {

                String[] addArra = address.split(":");

                if (addArra.length != 2) continue;

                if(addArra[0].trim().equals("127.0.0.1") || addArra[0].trim().equals("localhost")) addArra[0] = LunaUtils.gainLocalHostIp();///modify by neo on 2017.07.11

                InetSocketAddress isa = new InetSocketAddress(addArra[0].trim(), Integer.parseInt(addArra[1].trim()));

                resultList.add(isa);
            }
        }

        return resultList;
    }

}
