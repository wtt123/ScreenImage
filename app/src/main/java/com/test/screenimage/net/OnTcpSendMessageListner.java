package com.test.screenimage.net;

import java.io.IOException;

/**
 * Created by wt
 * Date on 2018/7/4
 */
public interface OnTcpSendMessageListner {
    void success(int mainCmd, int subCmd, String body, byte[] bytes);
    void error(Exception e);
}
