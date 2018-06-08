package com.test.screenimage.stream.sender.tcp.interf;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc 连接监听
 */

public interface TcpConnectListener {
    //socket连接成功
    void onSocketConnectSuccess();
    //socket连接失败
    void onSocketConnectFail();
    //tcp连接成功
    void onTcpConnectSuccess();
    //tcp连接失败
    void onTcpConnectFail();
    //发送成功
    void onPublishSuccess();
    //发送失败
    void onPublishFail();
    //socket断开连接
    void onSocketDisconnect();
}
