package com.test.screenimage.stream.sender.tcp;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.test.screenimage.constant.ScreenImageApi;
import com.test.screenimage.entity.ReceiveData;
import com.test.screenimage.entity.ReceiveHeader;
import com.test.screenimage.stream.sender.tcp.interf.OnTcpReadListener;
import com.test.screenimage.utils.AnalyticDataUtils;
import com.test.screenimage.utils.ByteUtil;
import com.test.screenimage.utils.SopCastLog;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc 处理流消息
 */

public class TcpReadThread extends Thread implements AnalyticDataUtils.OnAnalyticDataListener {
    private final static String TAG = "wt";
    private BufferedInputStream bis;
    private AnalyticDataUtils mAnalyticDataUtils;
    private OnTcpReadListener mListener;
    private volatile boolean startFlag;

    public TcpReadThread(BufferedInputStream bis) {
        this.bis = bis;
        mAnalyticDataUtils = new AnalyticDataUtils();
        mAnalyticDataUtils.setOnAnalyticDataListener(this);
        startFlag = true;
    }

    public void setOnTcpReadListener(OnTcpReadListener listener) {
        this.mListener = listener;
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
                if (mListener != null) mListener.socketDisconnect(e.getMessage());
            }
        }
    }


    // TODO: 2018/6/6 wt 接收到服务端发来的消息指令
    public void acceptMsg() throws IOException {
        if (mListener == null) {
            return;
        }
        if (bis.available() <= 0) {
            return;
        }
        byte[] header = mAnalyticDataUtils.readByte(bis, 18);
        //根据协议分析数据头
        ReceiveHeader receiveHeader = mAnalyticDataUtils.analysisHeader(header);
        if (receiveHeader.getStringBodylength() == 0 && receiveHeader.getBuffSize() == 0) {
            SopCastLog.e("wtt", "接收数据为空");
            return;
        }
        if (receiveHeader.getEncodeVersion() != ScreenImageApi.encodeVersion1) {
            SopCastLog.e("wtt", "收到的消息无法解析");
            return;
        }
        //解析数据
        mAnalyticDataUtils.analyticData(bis, receiveHeader);
    }

    /**
     * 停止读
     */
    public void shutDown() {
        startFlag = false;
        this.interrupt();
    }

    @Override
    public void onSuccess(ReceiveData data) {
        if (mListener != null) mListener.connectSuccess(data);
    }
}
