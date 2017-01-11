package com.pemc.crss.metering.actuate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

@Slf4j
@Component
public class SystemConfigInfo implements InfoContributor {

    private final CacheManager cacheManager;

    @Autowired
    public SystemConfigInfo(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Cache configCache = cacheManager.getCache("config");

        if (configCache != null) {
            log.debug("Displaying system configuration from cache.");

            builder.withDetail("Server ID", getServerID());
            builder.withDetail("System Configuration", configCache.getNativeCache());
        }
    }

    private String getServerID() {
        String retVal = "";

        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

            retVal = sb.toString();
        } catch (UnknownHostException | SocketException e) {
            log.error(e.getMessage(), e);
        }

        return retVal;
    }

}
