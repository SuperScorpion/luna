package com.jy.luna.xsd;

import com.jy.luna.stuff.common.LunaUtils;
import com.jy.luna.stuff.exception.LunaException;
import com.jy.luna.xsd.element.Cli;
import com.jy.luna.xsd.element.Registry;
import com.jy.luna.xsd.element.Sev;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by neo on 2017/6/28.
 */
public class LunaXsdHandler {

    public static String name;//Sev

    public static String port;//Sev

    public static String address;//Registry

    public static Boolean isRoundRobin = true;//Registry

//    public static List<String> servicePathList;//Cli

    public static Map<String, List<String>> serviceUrlListMap;//Cli

    public static Map<String, String> serviceTimeoutMap;//Cli



    public static Boolean initParamOfXml(ApplicationContext applicationContext) {

        Map<String, Registry> rmp = applicationContext.getBeansOfType(Registry.class);
        Map<String, Sev> smp = applicationContext.getBeansOfType(Sev.class);
        Map<String, Cli> cmp = applicationContext.getBeansOfType(Cli.class);



        if(cmp.isEmpty() && smp.isEmpty()) {
            throw new LunaException("Luna: The cli or sev the two tag must write one, check your xml please");
        } else if(!cmp.isEmpty() && !smp.isEmpty()) {
            throw new LunaException("Luna: Only one tag can exist, check your xml please");
        } else if(smp.size() > 1 && cmp.size() == 0) {
            throw new LunaException("Luna: Only one sev tag can exist, check your xml please");
        }

        //registry必须要
        if(rmp.isEmpty()) throw new LunaException("Luna: The registry:address tag is a need, check your xml please");

        boolean isServerFlag = smp.isEmpty() ? false : true;


        //server
        if(isServerFlag) {
            Sev sev= smp.values().iterator().next();
            name = sev.getName();
            port = sev.getPort();
            if (LunaUtils.isBlank(port)) throw new LunaException("Luna: Port is a need");
        } else {
            //client

            serviceTimeoutMap = new HashMap<>();
            serviceUrlListMap = new HashMap<>();

            for (Cli c : cmp.values()) {

                if(LunaUtils.isBlank(c.getService())) continue;

                serviceTimeoutMap.put(c.getService(), c.getTimeout());

                if(LunaUtils.isNotBlank(c.getUrl())) {
                    String[] urlArrays = LunaUtils.split(c.getUrl(), ",");
                    serviceUrlListMap.put(c.getService(), Arrays.asList(urlArrays));
                }
            }

        }



        //registry

        if(rmp.size() > 1) throw new LunaException("Luna: The registry tag is repeat, check your xml please");

        Registry ris = rmp.values().iterator().next();
        isRoundRobin = LunaUtils.isBlank(ris.getRoundRobin()) || !ris.getRoundRobin().equalsIgnoreCase("random") ? true : false;
        address = ris.getAddress();

        return isServerFlag;
    }
}
