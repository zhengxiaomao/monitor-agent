package com.example.monitoragent.tools;

import java.net.Inet4Address;
import java.net.InetAddress;

public class IpTool {

    public static String getIp() throws Exception{

            InetAddress ip4 = Inet4Address.getLocalHost();
            return ip4.getHostAddress();
    }
}
