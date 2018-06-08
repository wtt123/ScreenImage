package com.test.screenimage.stream.sender.tcp;

import android.util.Log;

import com.test.screenimage.entity.Frame;
import com.test.screenimage.stream.sender.sendqueue.ISendQueue;
import com.test.screenimage.stream.sender.tcp.interf.OnTcpWriteListener;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc
 */

public class TcpWriteThread extends Thread {
    private BufferedOutputStream bos;
    private ISendQueue iSendQueue;
    private volatile boolean startFlag;
    private OnTcpWriteListener mListener;
    private final String TAG = "TcpWriteThread";

    public TcpWriteThread(BufferedOutputStream bos, ISendQueue sendQueue, OnTcpWriteListener listener) {
        this.bos = bos;
        startFlag = true;
        this.iSendQueue = sendQueue;
        this.mListener = listener;
    }

    @Override
    public void run() {
        super.run();
        while (startFlag) {
            Frame frame = iSendQueue.takeFrame();
            if (frame == null) {
                continue;
            }
//            if (frame.data instanceof Video) {
//                sendData(((Video) frame.data).getData());
//            }
            // TODO: 2018/5/29 wt修改
            if (frame.data.length != 0) {
                sendData(frame.data);
                Log.e(TAG, "send a msg");
            }
        }
    }

    // TODO: 2018/6/4 wt 发送数据
    public void sendData(byte[] buff) {
        try {
            EncodeV1 encodeV1 = new EncodeV1(buff);
            bos.write(encodeV1.buildSendContent());
            bos.flush();
//            Log.e(TAG,"send data ");
        } catch (IOException e) {
            startFlag = false;
//            Log.e("TcpWriteThread", "sendData Exception =" + e.toString());
            mListener.socketDisconnect();
        }
    }

    /**
     * 停止写
     */
    public void shutDown() {
        startFlag = false;
        this.interrupt();
    }

}
