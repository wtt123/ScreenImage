package com.test.screenimage.net;

/**
 * Created by xu.wang
 * Date on 2016/11/11 09:48
 * tcp发送消息和文件,并接受回调的方法.
 */
public class TcpUtil {
    private static TcpUtil instance;
    private String ip;
    private int port;
    private int connectTime = 2000;

    private Type mType;

    private boolean isCancel = true;
    private RequestTcp requestTcp;

    private enum Type {
        DEFAULT, SETTING     //向pc发送,向设置ip发送
    }

    private TcpUtil() {
        mType = Type.DEFAULT;
//        this.ip = MobileTeach.pc_ip;
//        this.port = MobileTeach.pc_tcp_port;
    }


    public TcpUtil(String ip, int port) {
        mType = Type.SETTING;
        this.ip = ip;
        this.port = port;
    }

    private static TcpUtil getInstance() {
        instance = new TcpUtil();
        return instance;
    }

    public void sendMessage(int mainCmd, int subCmd, OnTcpSendMessageListner listner) {
        sendMessage(mainCmd, subCmd, null, listner);
    }

    public void sendMessage(int mainCmd, int subCmd, final String sendBody, OnTcpSendMessageListner listner) {
        sendMessage(mainCmd, subCmd, sendBody, new byte[0], connectTime, listner);
    }

    public void sendMessage(final int mainCmd, final int subCmd, final String sendBody, byte[] bytes, final int connectSoTime, final OnTcpSendMessageListner listner) {
        requestTcp = new RequestTcp(ip, port, mainCmd, subCmd, sendBody, bytes, connectSoTime, listner);
        requestTcp.start();
    }

    public void sendTimeOut(int timeOut) {
        this.connectTime = timeOut;
    }


    public void cancel() {
        if (requestTcp != null) {
            requestTcp.shutdown();
        }
    }

}
