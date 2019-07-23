package com.deilson.nsdbroadcast;

public interface NsdServiceCallback {
    void onServiceFound(
            String serviceType, String serviceName, String hostAddress,
            int hostPort, String serviceDescription);
    void onServiceLost(
            String serviceType, String serviceName, String hostAddress,
            int hostPort, String serviceDescription);
    void onError(Throwable t);
}
