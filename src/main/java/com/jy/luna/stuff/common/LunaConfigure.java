package com.jy.luna.stuff.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by neo on 2017/6/20.
 */
public class LunaConfigure {

    public static ExecutorService execuService = Executors.newCachedThreadPool();


    public static int ZK_SESSION_TIMEOUT = 9000;

    public static String ZK_REGISTRY_PATH = "/registry";
    public static String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
