package com.test.screenimage.stream.sender.tcp;

import android.text.TextUtils;
import android.util.Log;

import com.test.screenimage.entity.Frame;
import com.test.screenimage.stream.sender.sendqueue.ISendQueue;
import com.test.screenimage.stream.sender.tcp.interf.OnTcpWriteListener;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

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
    private volatile int readLength = 0;
    private Timer timer;
    private boolean isCalculate = false;

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
        startNetSpeedCalculate();
        while (startFlag) {
            Frame frame = iSendQueue.takeFrame();
            if (frame == null) {
                continue;
            }
            // TODO: 2018/5/29 wt修改
            if (frame.data.length != 0) {
                sendData(frame.data);
            }
        }
    }

    // TODO: 2018/6/4 wt 发送数据
    public void sendData(byte[] buff) {
        try {
            byte[] sendBuff = new EncodeV1(mainCmd, subCmd, sendBody, buff).buildSendContent();
            if (isCalculate) readLength += sendBuff.length;
            bos.write(sendBuff);
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
        mListener = null;
        isCalculate = false;
        try {
            if (timer != null) timer.cancel();
        } catch (Exception e) {
        }
        startFlag = false;
        this.interrupt();
    }

    public void sendStartBuff() {
        sendData(new byte[0]);
    }

    public void startNetSpeedCalculate() {
        try {
            if (timer != null) timer.cancel();
        } catch (Exception e) {
        }
        readLength = 0;
        isCalculate = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.netSpeedChange((readLength / 1024) + " kb/s");
                    readLength = 0;
                }
            }
        }, 1000, 1000);
    }
}
