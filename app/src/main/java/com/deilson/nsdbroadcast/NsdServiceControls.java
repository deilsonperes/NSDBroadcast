package com.deilson.nsdbroadcast;

import android.os.SystemClock;

import com.d_peres.easylogger.EasyLogger;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;

import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_ADDR;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_DESC;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_NAME;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_PORT;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_TYPE;

@SuppressWarnings({"CharsetObjectCanBeUsed", "FieldCanBeLocal", "unused"})
class NsdServiceControls {
    private EasyLogger log = new EasyLogger(NsdUtils.TAG, getClass());

    private String serviceName;
    private String serviceType;
    private String hostAddress;
    private String serviceDescription;
    private int    hostPort;

    private int broadcastInterval = 2000;

    private final Object threadStopLock = new Object();
    private boolean threadStopSignal = false;

    NsdServiceControls(
            String serviceType, String serviceName, String hostAddress,
            int hostPort, String serviceDescription
    ) {
        this.serviceType        = serviceType;
        this.serviceName        = serviceName;
        this.hostAddress        = hostAddress;
        this.hostPort           = hostPort;
        this.serviceDescription = serviceDescription;

        t_service.start();
    }

    private Thread t_service = new Thread(new Runnable() {
        @Override
        public void run() {
            log.i("NsdServiceBroadcast start");

            DatagramSocket socket;
            DatagramPacket packet;

            int port_a = 15500;
            int port_b = 14500;
            int port_c = 10500;

            JSONObject jsonMessage = new JSONObject();

            try {
                InterfaceAddress intfAddr = NsdUtils.getAddress();
                if(intfAddr == null) {
                    return;
                }

                socket = new DatagramSocket(null);
                socket.setBroadcast(true);
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(intfAddr.getAddress(), 0));

                jsonMessage.putOpt(JSON_S_TYPE, serviceType);
                jsonMessage.putOpt(JSON_S_NAME, serviceName);
                jsonMessage.putOpt(JSON_S_ADDR, hostAddress);
                jsonMessage.putOpt(JSON_S_PORT, hostPort);
                jsonMessage.putOpt(JSON_S_DESC, serviceDescription);

                packet = new DatagramPacket(new byte[512], 512);
                packet.setAddress(intfAddr.getBroadcast());
                packet.setData(jsonMessage.toString().getBytes("UTF-8"));

            } catch (Exception e) {
                log.e(e);
                return;
            }

            while (!threadStopSignal) {
                try {
                    packet.setPort(port_a);
                    socket.send(packet);
                    packet.setPort(port_b);
                    socket.send(packet);
                    packet.setPort(port_c);
                    socket.send(packet);
                    SystemClock.sleep(broadcastInterval);
                } catch (IOException e) {
                    log.e(e);
                    break;
                }
            }

            socket.close();

            synchronized (threadStopLock) {
                threadStopLock.notify();
            }

            log.i("NsdServiceBroadcast end");
        }
    });

    public void stopBroadcast() {
        synchronized (threadStopLock) {
            try {
                threadStopSignal = true;
                threadStopLock.wait();
            } catch (InterruptedException e) {
                log.e(e);
            }
        }
    }

    public void setBroadcastInterval(int intervalMs) {
        broadcastInterval = intervalMs;
    }
}
