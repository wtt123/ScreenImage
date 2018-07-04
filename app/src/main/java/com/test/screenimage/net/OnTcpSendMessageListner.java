package com.test.screenimage.net;

/**
 * Created by xu.wang
 * Date on 2016/11/11 10:52
 */
public interface OnTcpSendMessageListner {
    void success(int mainCmd, int subCmd, String body, byte[] bytes);
    void error(Exception e);
}
