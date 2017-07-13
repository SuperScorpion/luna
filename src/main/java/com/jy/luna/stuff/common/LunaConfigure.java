package com.jy.luna.stuff.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by neo on 2017/6/20.
 */
public class LunaConfigure {

    public static ExecutorService execuService = Executors.newCachedThreadPool();


    public static int FUTURE_ERROR_TIMES = 3;

    public static String FUTURE_ERROR_MSG = "luna_error";

    public static int ZK_SESSION_TIMEOUT = 6000;///ConnectionLossException: KeeperErrorCode = ConnectionLoss when i set breakpoints

    public static String ZK_REGISTRY_PATH = "/registry";
}
