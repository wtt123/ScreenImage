package com.test.screenimage.stream.sender.udp.interf;

/**
 * Created by wt on 2018/7/11.
 */
public interface OnUdpConnectListener {
    void udpConnectSuccess(String ip);
    void udpDisConnec(String message);
}
