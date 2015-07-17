package com.swiftnav.sbp.drivers;

import android.util.Log;

import com.swiftnav.sbp.client.SBPDriver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SBPDriverUDP implements SBPDriver {
    static final String TAG = "SBPDriverUDP";
    static final int RECV_SIZE = 256;
    static final int DGRAM_PORT = 2000;
    DatagramSocket socket;
    String server;
    byte[] rxdata;

    public SBPDriverUDP(String server_) {
        server = server_;
        rxdata = new byte[0];
    }

    @Override
    public byte[] read(int len) throws IOException {
        if (socket == null)
            openSocket();

        while (rxdata.length < len) {
            DatagramPacket packet = new DatagramPacket(new byte[RECV_SIZE], RECV_SIZE);
            socket.receive(packet);
            ByteBuffer bb = ByteBuffer.wrap(new byte[rxdata.length + packet.getLength()]);
            bb.put(rxdata);
            bb.put(packet.getData(), rxdata.length, packet.getLength());
            rxdata = bb.array();
        }
        byte[] ret = Arrays.copyOf(rxdata, len);
        rxdata = Arrays.copyOfRange(rxdata, len, rxdata.length - len);
        return ret;
    }

    @Override
    public void write(byte[] data) throws IOException {
        if (socket == null)
            openSocket();

        DatagramPacket packet = new DatagramPacket(data, data.length);
        socket.send(packet);
    }

    private void openSocket() throws IOException {
        try {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName(server), DGRAM_PORT);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setup socket");
            e.printStackTrace();
        }
    }
}