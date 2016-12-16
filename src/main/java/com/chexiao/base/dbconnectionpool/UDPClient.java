package com.chexiao.base.dbconnectionpool;

/**
 * Created by fulei on 2016-12-15.
 */
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class UDPClient {
    private static String ip;

    private static int port;


    /**
     * send udp msg
     * @param msg
     */
    public static void sendMsg(String msg) throws Exception{
        String serverIP = getIp();
        int serverPort = getPort();
        if(serverIP == null || serverIP.equals("") || serverPort == 0) {
            throw new Exception("ip or port is null");
        }
        sendMsg(msg, serverIP, serverPort);
    }

    /**
     * send udp msg
     * @param msg
     * @param ip
     * @param port
     */
    public static void sendMsg(String msg, String ip, int port) throws Exception {
        ByteArrayOutputStream bytesOut = null;
        DataOutputStream dataOut = null;
        DatagramSocket ds = null;
        try {
            bytesOut = new ByteArrayOutputStream();
            dataOut = new DataOutputStream(bytesOut);

            ds = new DatagramSocket();

            dataOut.writeBytes(msg);
            dataOut.flush();
            byte[] buffer =msg.getBytes("UTF8"); //bytesOut.toByteArray();
            DatagramPacket dp = new DatagramPacket(buffer,
                    buffer.length,
                    new InetSocketAddress(ip, port));
            ds.send(dp);
        } catch (Exception e) {
            throw e;
        } finally {
            if(dataOut != null) {
                try {
                    dataOut.close();
                } catch (IOException e) {
                    throw e;
                }
            }

            if(bytesOut != null) {
                try {
                    bytesOut.close();
                } catch (IOException e) {
                    throw e;
                }
            }

            if(ds != null) {
                ds.close();
            }
        }
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        UDPClient.ip = ip;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        UDPClient.port = port;
    }
}
