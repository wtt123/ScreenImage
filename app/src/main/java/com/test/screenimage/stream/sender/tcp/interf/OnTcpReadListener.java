package com.test.screenimage.stream.sender.tcp.interf;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc 从tcp read thread中的回调
 */

public interface OnTcpReadListener {

    void socketDisconnect();    //断开连接

    //收到server消息,连接成功
    //mainCmd:主指令 subCmd:子指令 body：消息内容
    void connectSuccess(short mainCmd, short subCmd, String body);
}
