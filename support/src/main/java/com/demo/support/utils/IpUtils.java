package com.demo.support.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 实例工具类
 */
public final class IpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);

    private static final String LOCAL_DEFAULT = "127.0.0.1";

    public static final String BROADCAST_IP = "0.0.0.0";

    /**
     * 工具函数保留空构造方法
     */
    private IpUtils() {
    }

    /**
     * 获取本地IPv4地址
     *
     * @return 地址名
     */
    public static String getLocalInet4Address() {
        String result = LOCAL_DEFAULT;
        Enumeration<NetworkInterface> interfaces;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.error("Error while get network interfaces", e);
            return result;
        }

        if (interfaces == null || !interfaces.hasMoreElements()) {
            return result;
        }

        do {
            NetworkInterface eth = interfaces.nextElement();
            Enumeration<InetAddress> group = eth.getInetAddresses();
            if (!group.hasMoreElements()) {
                continue;
            }

            do {
                InetAddress ip = group.nextElement();
                if (ip instanceof Inet4Address
                    && !ip.isLinkLocalAddress()
                    && !ip.isLoopbackAddress()) {
                    result = ip.getHostAddress();
                }
            } while (group.hasMoreElements());

        } while (interfaces.hasMoreElements());

        return result;
    }

}