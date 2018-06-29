package com.test.screenimage.stream.sender.tcp.interf;

/**
 * Created by wt
 * Date on  2018/5/28
 * 从tcp Write thread中的回调
 */

public interface OnTcpWriteListener {
    //断开连接
    void socketDisconnect(String message);
}
