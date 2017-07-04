package com.jy.luna.xsd;

import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.element.Cli;
import com.jy.luna.xsd.element.Registry;
import com.jy.luna.xsd.element.Sev;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by neo on 2017/6/28.
 */
public class LunaXsdHandler {

    public static String name;//Sev

    public static String port;//Sev

    public static String address = "localhost:2181";//Registry

    public static Boolean isRoundRobin;//Registry

    public static List<String> servicePathList;//Cli

    public static Map<String, String> serviceTimeoutMap;//Cli



    public static Boolean initParamOfXml(ApplicationContext applicationContext) {

        Map<String, Registry> rmp = applicationContext.getBeansOfType(Registry.class);
        Map<String, Sev> smp = applicationContext.getBeansOfType(Sev.class);
        Map<String, Cli> cmp = applicationContext.getBeansOfType(Cli.class);

        if(rmp.isEmpty()) throw new LunaException("Luna: registry tag is a need, check your xml please");

        if(cmp.isEmpty() && smp.isEmpty()) {
            throw new LunaException("Luna: cli or sev the two tag must write one, check your xml please");
        } else if(!cmp.isEmpty() && !smp.isEmpty()) {
            throw new LunaException("Luna: only one tag can exist, check your xml please");
        } else if(smp.size() > 1 && cmp.size() == 0) {
            throw new LunaException("Luna: only one sev tag can exist, check your xml please");
        }

        boolean isServerFlag = smp.isEmpty() ? false : true;

        Registry ris = rmp.values().iterator().next();

        //server
        if(isServerFlag) {
            Sev sev= smp.values().iterator().next();
            name = sev.getName();
            port = sev.getPort();
            if (LunaUtils.isBlank(port)) throw new LunaException("Luna: port is a need");
        } else {
            //client
            serviceTimeoutMap = new HashMap<>();
            servicePathList = new ArrayList<>();
            for (Cli c : cmp.values()) {
                serviceTimeoutMap.put(c.getService(), c.getTimeout());
                servicePathList.add(c.getService());
            }
        }

        //registry
        isRoundRobin = LunaUtils.isBlank(ris.getRoundRobin()) || !ris.getRoundRobin().equalsIgnoreCase("random") ? true : false;
        address = ris.getAddress();
        if(LunaUtils.isBlank(address)) throw new LunaException("Luna: address is a need");

        return isServerFlag;
    }
}
