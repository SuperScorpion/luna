package com.jy.luna.stuff;

import com.jy.luna.client.ClientStuff;
import com.jy.luna.server.ServerStuff;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.LunaXsdHandler;
import com.jy.luna.zookeeper.ServiceDiscovery;
import com.jy.luna.zookeeper.ServiceRegistry;
import org.springframework.context.ApplicationContext;

public class LunaSpringReform {

	public static void reform(ApplicationContext applicationContext) {

		if(applicationContext == null) throw new LunaException("Luna: spring applicationContext is null");


		boolean isServer = LunaXsdHandler.initParamOfXml(applicationContext);//初始化用户设置 xml参数值

		if(isServer)
			new ServerStuff(new ServiceRegistry(), applicationContext).connectServerProcessor();
		else
			new ServiceDiscovery(new ClientStuff().initProxy2Spring(applicationContext));
	}

}
