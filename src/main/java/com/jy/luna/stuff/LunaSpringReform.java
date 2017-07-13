package com.jy.luna.stuff;

import com.jy.luna.client.ClientCoreProcessor;
import com.jy.luna.client.ClientStuff;
import com.jy.luna.server.ServerStuff;
import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.LunaXsdHandler;
import com.jy.luna.zookeeper.ServiceDiscovery;
import com.jy.luna.zookeeper.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class LunaSpringReform {

	private static final Logger LOGGER = LoggerFactory.getLogger(LunaSpringReform.class);

	public static void reform(ApplicationContext applicationContext)  {

		if(applicationContext == null) throw new LunaException("Luna: The spring applicationContext is null");

		boolean isServer = LunaXsdHandler.initParamOfXml(applicationContext);//初始化用户设置 xml参数值

		if(isServer) {
			if (LunaUtils.isBlank(LunaXsdHandler.address)) {//直连方式sev
				//// TODO: 2017/7/12
				LOGGER.debug("Luna: Server start direct connection mode.");
				new ServerStuff(null, applicationContext).connectServerProcessor();//sev
			} else {
				LOGGER.debug("Luna: Client try to add zookeeper server node.");
				new ServerStuff(new ServiceRegistry(), applicationContext).connectServerProcessor();//sev
			}
		} else {

			ClientStuff csf = new ClientStuff().initProxy2Spring(applicationContext);//先初始化客户端代理

			if (LunaUtils.isBlank(LunaXsdHandler.address)) {//直连方式cli

				LOGGER.debug("Luna: Client start direct connection mode.");
				for (String serviceFullName : LunaXsdHandler.serviceTimeoutMap.keySet()) {
					try {
						LOGGER.debug("Luna: " + serviceFullName + " try to add direct server node.");
						ClientCoreProcessor.getInstance().refreshLocalServerByThisService(serviceFullName, LunaXsdHandler.serviceUrlListMap.get(serviceFullName), csf);
					} catch (Exception e) {
						LOGGER.error("Luna: ", e);
						e.printStackTrace();
					}
				}
			} else {//zookeeper 方式cli
				LOGGER.debug("Luna: Server try to add zookeeper server node.");
				new ServiceDiscovery(csf);
			}
		}
	}
}
