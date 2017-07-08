package com.jy.luna.stuff.common;


import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by neo on 2016/12/4.
 */
public class LunaUtils {

    public static Boolean isBlank(CharSequence cs) {

        int strLen;

        if(cs != null && (strLen = cs.length()) != 0) {

            for(int i = 0; i < strLen; ++i) {
                if(!Character.isWhitespace(cs.charAt(i))) return false;
            }

            return true;

        } else {

            return true;
        }
    }

    public static Boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    public static String gainLocalHostIp() {

        Enumeration allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress ip;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            Enumeration addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = (InetAddress) addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address) {
                    if(netInterface.getName() != null && netInterface.getName().equalsIgnoreCase("en0")) return ip.getHostAddress();
                }
            }
        }

        return "";
    }

    /**
     * 把输入字符串的首字母改成小写
     * @param str
     * @return
     */
    public static String lowcaseFirst(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }







/*
    public static void main(String[] args) throws UnknownHostException, SocketException {
//        ServerStuff sstf = new ServerStuff();
        try {
//            sstf.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        InetSocketAddress isa = new InetSocketAddress("localhost", 3333);
        InetSocketAddress isa2 = new InetSocketAddress("localhost", 3333);

        LunaConfigure sf = new LunaConfigure();
        LunaConfigure sf2 = new LunaConfigure();

        List<LunaConfigure> pc = new ArrayList<>();
        pc.add(sf);
        System.out.println(pc.contains(sf2));

        List<InetSocketAddress> p = new ArrayList<>();
        p.add(isa);

        System.out.println(isa == isa2);
        System.out.println(p.contains(isa2));

        System.out.println(isa.toString());

        System.out.println(isa.getHostName() + ":" + isa.getPort());

        System.out.println(isa.getHostString());
        System.out.println(LunaUtils.gainLocalHostIp());
//        Map fd = new HashMap();
//        fd.put("a", "1");
//
//        Set vb = new HashSet(fd.keySet());
//        System.out.println(vb.size());
//
//        fd.put("b", "2");
//        System.out.println(vb.size());

    }*/
}
