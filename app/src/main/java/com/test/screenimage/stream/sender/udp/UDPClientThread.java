package com.test.screenimage.stream.sender.udp;

import android.os.Message;
import android.util.Log;

import com.test.screenimage.MyApplication;
import com.test.screenimage.stream.sender.udp.interf.OnUdpConnectListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by wt on 2018/7/11.
 * 基于udp的组网连接
 */
public class UDPClientThread extends Thread {
    static final String BROADCAST_IP = "224.0.0.1";
    //监听的端口号
    static final int BROADCAST_PORT = 15000;
    private InetAddress inetAddress = null;
    //服务端的局域网IP
    private static String ip;
    private OnUdpConnectListener mListener;

    public UDPClientThread(OnUdpConnectListener listener) {
        this.mListener = listener;
        this.start();
    }

    @Override
    public void run() {
        MulticastSocket multicastSocket = null;//多点广播套接字
        try {
            /**
             * 1.实例化MulticastSocket对象，并指定端口
             * 2.加入广播地址，MulticastSocket使用public void joinGroup(InetAddress mcastaddr)
             * 3.开始接收广播
             * 4.关闭广播
             */
            multicastSocket = new MulticastSocket(BROADCAST_PORT);
            inetAddress = InetAddress.getByName(BROADCAST_IP);
            Log.e("UdpClientThread", "udp server start");
            multicastSocket.joinGroup(inetAddress);
            byte buf[] = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            while (true) {
                multicastSocket.receive(dp);
                Log.e("UdpClientThread", "receive a msg");
                ip = new String(buf, 0, dp.getLength());
                multicastSocket.leaveGroup(inetAddress);
                multicastSocket.close();
                MyApplication.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.udpConnectSuccess(ip);
                    }
                });
            }
        } catch (Exception e) {
            MyApplication.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.udpDisConnec(e.getMessage());
                }
            });
        } finally {
            Log.e("UdpClientThread", "udp server close");
        }
    }
}
