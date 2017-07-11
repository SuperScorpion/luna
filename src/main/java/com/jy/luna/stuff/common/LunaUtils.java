package com.jy.luna.stuff.common;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
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


    //commons lang3 copy
    public static String[] split(String str, String separatorChars) {
        return splitWorker(str, separatorChars, -1, false);
    }

    private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
        if(str == null) {
            return null;
        } else {
            int len = str.length();
            if(len == 0) {
                return new String[0];
            } else {
                ArrayList list = new ArrayList();
                int sizePlus1 = 1;
                int i = 0;
                int start = 0;
                boolean match = false;
                boolean lastMatch = false;
                if(separatorChars != null) {
                    if(separatorChars.length() != 1) {
                        label87:
                        while(true) {
                            while(true) {
                                if(i >= len) {
                                    break label87;
                                }

                                if(separatorChars.indexOf(str.charAt(i)) >= 0) {
                                    if(match || preserveAllTokens) {
                                        lastMatch = true;
                                        if(sizePlus1++ == max) {
                                            i = len;
                                            lastMatch = false;
                                        }

                                        list.add(str.substring(start, i));
                                        match = false;
                                    }

                                    ++i;
                                    start = i;
                                } else {
                                    lastMatch = false;
                                    match = true;
                                    ++i;
                                }
                            }
                        }
                    } else {
                        char sep = separatorChars.charAt(0);

                        label71:
                        while(true) {
                            while(true) {
                                if(i >= len) {
                                    break label71;
                                }

                                if(str.charAt(i) == sep) {
                                    if(match || preserveAllTokens) {
                                        lastMatch = true;
                                        if(sizePlus1++ == max) {
                                            i = len;
                                            lastMatch = false;
                                        }

                                        list.add(str.substring(start, i));
                                        match = false;
                                    }

                                    ++i;
                                    start = i;
                                } else {
                                    lastMatch = false;
                                    match = true;
                                    ++i;
                                }
                            }
                        }
                    }
                } else {
                    label103:
                    while(true) {
                        while(true) {
                            if(i >= len) {
                                break label103;
                            }

                            if(Character.isWhitespace(str.charAt(i))) {
                                if(match || preserveAllTokens) {
                                    lastMatch = true;
                                    if(sizePlus1++ == max) {
                                        i = len;
                                        lastMatch = false;
                                    }

                                    list.add(str.substring(start, i));
                                    match = false;
                                }

                                ++i;
                                start = i;
                            } else {
                                lastMatch = false;
                                match = true;
                                ++i;
                            }
                        }
                    }
                }

                if(match || preserveAllTokens && lastMatch) {
                    list.add(str.substring(start, i));
                }

                return (String[])list.toArray(new String[list.size()]);
            }
        }
    }





   /* public static void main(String[] args) throws UnknownHostException, SocketException {

        *//*InetSocketAddress isa = new InetSocketAddress("localhost", 3333);
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
        System.out.println(LunaUtils.gainLocalHostIp());*//*

        String s = "1,2,3,4,5,6,";
        String[] c = split(s, ",");

        for(String x : c) {
            System.out.println(x);
        }

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
