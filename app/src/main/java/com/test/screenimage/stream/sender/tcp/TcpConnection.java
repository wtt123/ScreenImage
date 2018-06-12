package com.test.screenimage.stream.sender.tcp;

import android.util.Log;

import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.entity.ReceiveData;
import com.test.screenimage.stream.sender.sendqueue.ISendQueue;
import com.test.screenimage.stream.sender.tcp.interf.OnTcpReadListener;
import com.test.screenimage.stream.sender.tcp.interf.OnTcpWriteListener;
import com.test.screenimage.stream.sender.tcp.interf.TcpConnectListener;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by wt
 * Date on  2018/5/28
 *
 * @Desc socket/tcp 连接
 */

public class TcpConnection implements OnTcpReadListener, OnTcpWriteListener {
    private TcpConnectListener listener;
    private static final String TAG = "TcpConnection";
    private Socket socket;
    private ISendQueue mSendQueue;
    private TcpWriteThread mWrite;
    private TcpReadThread mRead;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private int width, height;
    private int maxBps;
    private int fps;
    private byte[] mSpsPps;


    public void setConnectListener(TcpConnectListener listener) {
        this.listener = listener;
    }

    public void setSendQueue(ISendQueue sendQueue) {
        mSendQueue = sendQueue;
    }

    //连接服务端
    public void connect(String ip, int port, int mainCmd, int subCmd, String sendBody) {
        socket = new Socket();
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        try {
            socket.connect(socketAddress, 20000);
            //tcp连接成功后is.read阻塞多长时间
            socket.setSoTimeout(60000);
        } catch (IOException e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onSocketConnectFail();
                return;
            }
        }
        listener.onSocketConnectSuccess();
        if (listener == null || socket == null || !socket.isConnected()) {
            listener.onSocketConnectFail();
            return;
        }
        try {
            // 获取当前连接的输出流
            out = new BufferedOutputStream(socket.getOutputStream());
            // 获取当前连接的输入流
            in = new BufferedInputStream(socket.getInputStream());
            mWrite = new TcpWriteThread(out, mSendQueue, mainCmd, subCmd, sendBody, this);
            mRead = new TcpReadThread(in, this);
            mRead.start();
            listener.onTcpConnectSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            listener.onTcpConnectFail();
        }
    }

    public void setVideoParams(VideoConfiguration videoConfiguration) {
        width = videoConfiguration.width;
        height = videoConfiguration.height;
        fps = videoConfiguration.fps;
        maxBps = videoConfiguration.maxBps;
    }

    public void setSpsPps(byte[] spsPps) {
        this.mSpsPps = spsPps;
    }

    @Override
    public void socketDisconnect() {
        //与服务端连接断开
        listener.onSocketDisconnect();
    }

    @Override
    public void connectSuccess(ReceiveData data) {
        //收到数据后，解析后得到数据
        Log.e("wtt", "connectSuccess: "+data.getHeader().getSubCmd() );
        if (data==null){
            return;
        }
        int subCmd=data.getHeader().getSubCmd();
        switch (subCmd) {
            case 0x01:
                //连接成功，开启发送线程
                mWrite.start();
                break;
        }
    }




    public void stop() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                if (mWrite != null) {
                    mWrite.shutDown();
                }
                if (mRead != null) {
                    mRead.shutDown();
                }
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clearSocket();
            }
        }.start();
    }

    private void clearSocket() {
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                socket = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
