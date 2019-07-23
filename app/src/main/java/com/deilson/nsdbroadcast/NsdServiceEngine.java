package com.deilson.nsdbroadcast;

public class NsdServiceEngine {

    public static NsdServiceControls publishService(
            String serviceType,
            String serviceName,
            String hostAddress,
            int hostPort,
            String serviceDescription) {

        return new NsdServiceControls(
                serviceType,
                serviceName,
                hostAddress,
                hostPort,
                serviceDescription);
    }

    public static NsdServiceFinder findService(String serviceType, NsdServiceCallback callback) {
        return new NsdServiceFinder(serviceType, callback);
    }
}
