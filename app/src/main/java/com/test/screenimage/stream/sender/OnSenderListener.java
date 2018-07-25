package com.test.screenimage.stream.sender;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc 发送监听
 */

public interface OnSenderListener {
      void onConnecting();

      void onConnected();

      void onDisConnected(String message);

      void onConnectFail(String message);

      void onPublishFail();

      void onNetGood();

      void onNetBad();

      // TODO: 2018/7/25 手动断开连接
      void shutDown();

    void netSpeedChange(String netSpeedMsg);

}
