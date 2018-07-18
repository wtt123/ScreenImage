package com.test.screenimage.stream.sender.tcp;

import android.text.TextUtils;
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
    private int mainCmd;
    private int subCmd;
    private String sendBody;
    private OnTcpWriteListener mListener;
    private final String TAG = "TcpWriteThread";
    private boolean isSendMsg = false;

    /**
     * by wt
     *
     * @param bos       输入流
     * @param sendQueue
     * @param mainCmd   主指令
     * @param subCmd    子指令
     * @param sendBody  文本消息内容
     */
    public TcpWriteThread(BufferedOutputStream bos, ISendQueue sendQueue, int mainCmd,
                          int subCmd, String sendBody) {
        this.bos = bos;
        this.iSendQueue = sendQueue;
        this.mainCmd = mainCmd;
        this.subCmd = subCmd;
        this.sendBody = sendBody;
        startFlag = true;
    }

    public void setOnTcpWriteThread(OnTcpWriteListener listener) {
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
            // TODO: 2018/5/29 wt修改
            if (frame.data.length != 0) {
                sendData(frame.data);
                if (frame.frameType == Frame.FRAME_TYPE_CONFIGURATION) {
                    Log.e(TAG, "send sps pps");
                } else {
                    Log.i(TAG, "send normal data");
                }
            }
        }
    }

    // TODO: 2018/6/4 wt 发送数据
    public void sendData(byte[] buff) {
        try {
            EncodeV1 encodeV1= new EncodeV1(mainCmd, subCmd, sendBody, buff);
            bos.write(encodeV1.buildSendContent());
            bos.flush();
//            Log.e(TAG,"send data ");
        } catch (IOException e) {
            startFlag = false;
            if (mListener != null) mListener.socketDisconnect(e.getMessage());
        }
    }

    /**
     * 停止写
     */
    public void shutDown() {
        startFlag = false;
        this.interrupt();
    }

    public void sendStartBuff() {
        sendData(new byte[0]);
    }
}
