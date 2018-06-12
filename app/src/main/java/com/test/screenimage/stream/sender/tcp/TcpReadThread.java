package com.test.screenimage.stream.sender.tcp;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.test.screenimage.constant.ScreenImageApi;
import com.test.screenimage.stream.sender.tcp.interf.OnTcpReadListener;
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


    // TODO: 2018/6/6 wt 接收到服务端发来的消息指令
    public void acceptMsg() throws IOException {
        if (mListener == null) {
            return;
        }
        if (bis.available() <= 0) {
            return;
        }
        byte[] bytes = readByte(bis, 18);
        byte netVersion = bytes[0];
        if (netVersion == ScreenImageApi.encodeVersion1) {
            netForV1(bis, mListener, bytes);
        }  else {
            SopCastLog.e("wtt", "收到的消息无法解析");
        }

//        if (mListener == null) {
//            return;
//        }
//        if (bis.available() <= 0) {
//            return;
//        }
//        byte[] bytes = new byte[2];
//        bis.read(bytes);
//        String s = new String(bytes);
//        if (TextUtils.isEmpty(s)) {
//            return;
//        }
//        if (TextUtils.equals(s, "OK")) {
//            mListener.connectSuccess();
//        }
    }

    // TODO: 2018/6/11 wt处理协议相应指令
    private void netForV1(BufferedInputStream bis, OnTcpReadListener listener, byte[] bytes)
            throws IOException {

        //实现数组之间的复制
        //bytes：源数组
        //srcPos：源数组要复制的起始位置
        //dest：目的数组
        //destPos：目的数组放置的起始位置
        //length：复制的长度
        byte[] buff = new byte[4];
        System.arraycopy(bytes, 1, buff, 0, 4);
        final short mainCmd = ByteUtil.bytesToShort(buff);       //主指令  1`5
        buff=new byte[4];
        System.arraycopy(bytes, 5, buff, 0, 4);
        final short subCmd = ByteUtil.bytesToShort(buff);    //子指令  5`9
        buff = new byte[4];
        System.arraycopy(bytes, 9, buff, 0, 4);
        int stringBodySize = ByteUtil.bytesToInt(buff);//文本数据 9 ~ 13;
        buff = new byte[4];
        System.arraycopy(bytes, 13, buff, 0, 4);
        int byteBodySize = ByteUtil.bytesToInt(buff);//byte数据 13^17

        buff = new byte[2 * 1024];
        int len = 0;
        int totalLen = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((len = bis.read(buff)) != -1) {
            if (len != -1) totalLen += len;
            baos.write(buff, 0, len);
            if (totalLen >= stringBodySize) {
                break;
            }
        }
        final String body = baos.toString();
        baos.close();
        if (!TextUtils.isEmpty(body)){
            mListener.connectSuccess(mainCmd,subCmd,body);
        }
    }

    /**
     * 保证从流里读到指定长度数据
     *
     * @param is
     * @param readSize
     * @return
     * @throws Exception
     */
    private byte[] readByte(InputStream is, int readSize) throws IOException {
        byte[] buff = new byte[readSize];
        int len = 0;
        int eachLen = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (len < readSize) {
            eachLen = is.read(buff);
            if (eachLen != -1) {
                len += eachLen;
                baos.write(buff, 0, eachLen);
            } else {
                break;
            }
            if (len < readSize) {
                buff = new byte[readSize - len];
            }
        }
        byte[] b = baos.toByteArray();
        baos.close();
        return b;
    }

    /**
     * 停止读
     */
    public void shutDown() {
        startFlag = false;
        this.interrupt();
    }
}
