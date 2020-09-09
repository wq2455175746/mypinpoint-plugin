package com.navercorp.pinpoint.plugin.bigo.brpc.interceptor;

import com.baidu.brpc.protocol.Request;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by whg on 7/29/2020.
 */
public class Utlis {

    public static String getRemoteIp(Request request) {
        String localIp = "unKnown";
        Map<String, String> tMap = getHost(request);
        return !tMap.isEmpty() ? tMap.get("remoteIp") : localIp;
    }

    public static String getRemoteHost(Request request) {
        String localHost = "unKnown";
        Map<String, String> tMap = getHost(request);
        return !tMap.isEmpty() ? tMap.get("remoteHost") : localHost;
    }

    public static String getLocalIp(Request request) {
        String localIp = "127.0.0.1";
        Map<String, String> tMap = getHost(request);
        return !tMap.isEmpty() ? tMap.get("localIp") : localIp;
    }

    public static String getLocalHost(Request request) {
        String localHost = "localhost";
        Map<String, String> tMap = getHost(request);
        return !tMap.isEmpty() ? tMap.get("localhost") : localHost;
    }

    public static Map<String, String> getHost(Request request) {
        Map<String, String> tMap = new HashMap<String, String>();
        if (null != request.getChannel()) {
            //--local
            InetSocketAddress localAddress = (InetSocketAddress) request.getChannel().localAddress();
            String localIp = localAddress.getAddress().getHostAddress();
            int localPort = localAddress.getPort();
            tMap.put("localIp", localIp);
            tMap.put("localhost", localIp + ":" + localPort);
            //--remote
            InetSocketAddress remoteAddress = (InetSocketAddress) request.getChannel().remoteAddress();
            String remoteIp = remoteAddress.getAddress().getHostAddress();
            int remotePort = remoteAddress.getPort();
            tMap.put("remoteIp", remoteIp);
            tMap.put("remoteHost", remoteIp + ":" + remotePort);
        }
        return tMap;
    }

}
