package com.leaf.common.utils;

import com.leaf.common.UnresolvedAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetUtils {

    public static String getLocalHost() {
        String hostAddress = null;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostAddress;
    }

    public static UnresolvedAddress[] spiltAddress(String address) {
        String[] add = address.split(",");
        UnresolvedAddress[] unresolvedAddresses = new UnresolvedAddress[add.length];

        for (int i = 0; i < add.length; i++) {
            String[] a = add[i].split("[:]");
            UnresolvedAddress unresolvedAddress = new UnresolvedAddress(a[0], Integer.valueOf(a[1]));
            unresolvedAddresses[i] = unresolvedAddress;
        }
        return unresolvedAddresses;
    }
}
