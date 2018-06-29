package com.test.screenimage.stream.sender.tcp.interf;

import com.test.screenimage.entity.ReceiveData;
import com.test.screenimage.entity.ReceiveHeader;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc 从tcp read thread中的回调
 */

public interface OnTcpReadListener {

    void socketDisconnect(String message);    //断开连接

    //收到server消息,连接成功
    //date:解析后的数据包
    void connectSuccess(ReceiveData data);
}
