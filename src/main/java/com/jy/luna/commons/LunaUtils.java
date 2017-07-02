package com.jy.luna.commons;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

    public static void main(String[] args) throws ClassNotFoundException {
        Class c= Class.forName("com.jy.luna.Label");
        System.out.println(c.getSimpleName());
    }
}
