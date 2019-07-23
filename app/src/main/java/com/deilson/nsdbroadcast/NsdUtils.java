package com.deilson.nsdbroadcast;

import com.d_peres.easylogger.EasyLogger;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

class NsdUtils {
    static final String TAG = "NSDB";
    private static EasyLogger log = new EasyLogger(TAG, "NsdUtils");


    static final String JSON_S_ADDR = "addr";
    static final String JSON_S_DESC = "desc";
    static final String JSON_S_NAME = "name";
    static final String JSON_S_PORT = "port";
    static final String JSON_S_TYPE = "type";

    static InterfaceAddress getAddress() {
        try {

            Enumeration<NetworkInterface> intfList = NetworkInterface.getNetworkInterfaces();
            if(intfList == null) {
                log.e("No network interface found!");
                return null;
            }

            while (intfList.hasMoreElements()) {
                NetworkInterface intf = intfList.nextElement();

                List<InterfaceAddress> intfAddrs = intf.getInterfaceAddresses();
                for(InterfaceAddress intfAddr : intfAddrs) {
                    InetAddress addr = intfAddr.getBroadcast();
                    if(addr != null)
                        return intfAddr;
                }
            }

            log.e("Broadcast address not found!");
            return null;
        } catch (SocketException e) {
            log.e(e);
            return null;
        }
    }
}
