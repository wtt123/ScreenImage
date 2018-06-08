package com.test.screenimage.stream.sender.tcp;

import android.os.SystemClock;
import android.text.TextUtils;

import com.test.screenimage.stream.sender.tcp.interf.OnTcpReadListener;


import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc
 */

public class TcpReadThread extends Thread {
    private final static String TAG = "TcpReadThread";
    private BufferedInputStream bis;
    private OnTcpReadListener mListener;
    private volatile boolean startFlag;

    public TcpReadThread(BufferedInputStream bis, OnTcpReadListener listener) {
        this.bis = bis;
        this.mListener = listener;
        startFlag = true;
    }

    @Override
    public void run() {
        super.run();
        while (startFlag) {
            SystemClock.sleep(50);
            try {
                acceptMsg();
            } catch (IOException e) {
                startFlag = false;
                mListener.socketDisconnect();
//                Log.e(TAG, "read data Exception = " + e.toString());
            }
        }
    }


    // TODO: 2018/6/6 wt 接收到服务端发来的ok消息指令
    public void acceptMsg() throws IOException {
        if (mListener == null) {
            return;
        }
        if (bis.available() <= 0) {
            return;
        }
        byte[] bytes = new byte[2];
        bis.read(bytes);
        String s = new String(bytes);
        if (TextUtils.isEmpty(s)) {
            return;
        }
        if (TextUtils.equals(s, "OK")) {
            mListener.connectSuccess();
        }
    }

    /**
     * 停止读
     */
    public void shutDown() {
        startFlag = false;
        this.interrupt();
    }
}
