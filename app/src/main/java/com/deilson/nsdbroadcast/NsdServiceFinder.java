package com.deilson.nsdbroadcast;

import com.d_peres.easylogger.EasyLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_ADDR;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_DESC;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_NAME;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_PORT;
import static com.deilson.nsdbroadcast.NsdUtils.JSON_S_TYPE;
import static com.deilson.nsdbroadcast.NsdUtils.TAG;

@SuppressWarnings({"CharsetObjectCanBeUsed", "FieldCanBeLocal"})
class NsdServiceFinder {
    private EasyLogger log = new EasyLogger(TAG, getClass());

    private String serviceType;
    private NsdServiceCallback callback;

    private boolean threadStopSignal = false;
    private final Object threadStopLock = new Object();

    NsdServiceFinder(
            String serviceType,
            NsdServiceCallback callback) {

        this.serviceType = serviceType;
        this.callback = callback;
        t_main.start();
    }

    private Thread t_main = new Thread(new Runnable() {
        @Override
        public void run() {
            log.i("NsdServiceFinder start");

            DatagramSocket socket;
            DatagramPacket packet;
            JSONObject jsonMessage;

            try {
                int[] ports = {15500, 14500, 10500};

                InterfaceAddress intfAddr = NsdUtils.getAddress();
                if(intfAddr == null) {
                    log.e("Broadcast address not found!");
                    return;
                }

                socket = new DatagramSocket(null);
                socket.setBroadcast(true);
                socket.setReuseAddress(true);
                socket.setSoTimeout(100);

                for (int i = 0; true; i++) {
                    try {
                        socket.bind(new InetSocketAddress(
                                intfAddr.getBroadcast(),
                                ports[i]));
                        break;
                    } catch (Exception e) {
                        if(i == 2) {
                            log.e("Could not find available port!");
                            return;
                        }
                    }
                }

                packet = new DatagramPacket(new byte[512], 512);
            } catch (Exception e) {
                log.e(e);
                return;
            }

            while (!threadStopSignal) {
                try {
                    Arrays.fill(packet.getData(), (byte) 0);
                    socket.receive(packet);
                    jsonMessage = new JSONObject(new String(packet.getData(), "UTF-8"));
                    log.i("Received: '%s'", jsonMessage);

                    if(jsonMessage.has(JSON_S_TYPE) && jsonMessage.getString(JSON_S_TYPE).equals(serviceType)) {
                        callback.onServiceFound(
                                jsonMessage.getString(JSON_S_TYPE),
                                jsonMessage.getString(JSON_S_NAME),
                                jsonMessage.getString(JSON_S_ADDR),
                                jsonMessage.getInt(JSON_S_PORT),
                                jsonMessage.getString(JSON_S_DESC)
                        );
                    }
                } catch (SocketTimeoutException e) {
                    // ignore
                } catch (JSONException e) {
                    log.i("json exception raised: %s", e.getMessage());
                } catch (Exception e) {
                    log.e(e);
                    break;
                }
            }

            synchronized (threadStopLock) {
                threadStopLock.notify();
            }

            log.i("NsdServiceFinder end");
        }
    });

    public void stopSearch() {
        synchronized (threadStopLock){
            try {
                threadStopSignal = true;
                threadStopLock.wait();
            } catch (InterruptedException e) {
                log.e(e);
            }
        }
    }
}
