package com.test.screenimage.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.test.screenimage.R;
import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.constant.ScreenImageApi;
import com.test.screenimage.controller.StreamController;
import com.test.screenimage.controller.audio.NormalAudioController;
import com.test.screenimage.controller.video.ScreenVideoController;
import com.test.screenimage.stream.packer.TcpPacker;
import com.test.screenimage.stream.sender.OnSenderListener;
import com.test.screenimage.stream.sender.tcp.TcpSender;
import com.test.screenimage.ui.ScreenImageActivity;
import com.test.screenimage.utils.ToastUtils;

/**
 * Created by wt
 * Date on  2018/7/20 16:14:02.
 *
 * @Desc
 */

public class ScreenImageService extends Service implements OnSenderListener {
    private static final String TAG = "ScreenRecorderService";

    private TcpSender tcpSender;
    private VideoConfiguration mVideoConfiguration;
    private StreamController mStreamController;
    private OnSenderListener mListener;
    private int mCurrentBps;
    private int netBodCount = 0;

    private static final int foregroundId = 1234;
    NotificationManager notificationManager;
    Notification notification;
    String id = "screen_image_channel";
    String name = "ScreenImage";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenImageBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
        }
        createNotification("....");

    }

    private void createNotification(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this)
                    .setChannelId(id)
                    .setContentTitle("ScreenImage 正在投屏中")
                    .setContentText(text)
                    .setContentIntent(getDefalutIntent(Notification.FLAG_FOREGROUND_SERVICE))
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle("ScreenImage 正在投屏中")
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(getDefalutIntent(Notification.FLAG_FOREGROUND_SERVICE))
                    .setOngoing(true);
            notification = notificationBuilder.build();
        }
        notificationManager.notify(foregroundId, notification);
    }

    private PendingIntent getDefalutIntent(int flags) {
        Intent intent = new Intent(this, ScreenImageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, flags);
        return pendingIntent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(foregroundId, notification);
        return START_NOT_STICKY;

    }

    @Override
    public void onConnecting() {
        if (mListener != null) mListener.onConnecting();
    }

    @Override
    public void onConnected() {
        mCurrentBps = mVideoConfiguration.maxBps;
        if (mListener != null) mListener.onConnected();
    }

    @Override
    public void onDisConnected(String message) {
        if (mListener != null) mListener.onDisConnected(message);
    }

    @Override
    public void onConnectFail(String message) {
        if (mListener != null) mListener.onConnectFail(message);
    }

    @Override
    public void onPublishFail() {
        if (mListener != null) mListener.onPublishFail();
    }

    @Override
    public void onNetGood() {
        //网络好
        netBodCount = 0;    //
//            LogUtil.e(TAG, "onNetGood Current Bps: " + mCurrentBps);
        if (mCurrentBps == mVideoConfiguration.maxBps) {
            return;
        }
        int bps;
        if (mCurrentBps + 100 <= mVideoConfiguration.maxBps) {
            bps = mCurrentBps + 100;
        } else {
            bps = mVideoConfiguration.maxBps;
        }
        boolean result = mStreamController.setVideoBps(bps);
        if (result) {
            mCurrentBps = bps;
        }
        if (mListener != null) mListener.onNetGood();
    }

    @Override
    public void onNetBad() {
        if (mCurrentBps == mVideoConfiguration.minBps) {
            netBodCount++;
            if (netBodCount >= 2) {
                netBodCount = 0;
            }
            return;
        }
        int bps;
        if (mCurrentBps - 550 >= mVideoConfiguration.minBps) {
            bps = mCurrentBps - 550;
        } else {
            bps = mVideoConfiguration.minBps;
        }
        boolean result = mStreamController.setVideoBps(bps);
        if (result) {
            mCurrentBps = bps;
        }
        if (mListener != null) mListener.onNetBad();
    }

    @Override
    public void shutDown() {
        if (mListener != null) {
            mListener.shutDown();
        }
    }

    @Override
    public void netSpeedChange(String netSpeedMsg) {
        createNotification(netSpeedMsg);
        if (mListener != null) mListener.netSpeedChange(netSpeedMsg);
    }

    public class ScreenImageBinder extends Binder {

        public ScreenImageService getService() {
            return ScreenImageService.this;
        }
    }

    /**
     * 开始录制屏幕
     * @param mMediaProjectionManage
     * @param resultCode
     * @param data
     * @param listener
     * @param ip
     * @param port
     */
    public void startController(MediaProjectionManager mMediaProjectionManage, int resultCode,
                                Intent data, OnSenderListener listener, String ip, int port) {
        ScreenVideoController screenVideoController = new ScreenVideoController(mMediaProjectionManage, resultCode, data);
        NormalAudioController audioController = new NormalAudioController(this);
        mStreamController = new StreamController(screenVideoController, audioController);
        TcpPacker packer = new TcpPacker();
        mListener = listener;
        tcpSender = new TcpSender(ip, port);
        tcpSender.setSenderListener(this);
        tcpSender.setMianCmd(ScreenImageApi.RECORD.MAIN_CMD);
        tcpSender.setSubCmd(ScreenImageApi.RECORD.RECORDER_REQUEST_START);
        tcpSender.setSendBody(Build.MODEL);
        mVideoConfiguration = new VideoConfiguration.Builder().setSize(1080, 1920).build();
        mStreamController.setVideoConfiguration(mVideoConfiguration);
        mStreamController.setPacker(packer);
        mStreamController.setSender(tcpSender);
        mStreamController.start();
        tcpSender.openConnect();
    }

//    @Override
//    public void unbindService(ServiceConnection conn) {
//        Log.e("ttt", "unbindService: zzz" );
//        super.unbindService(conn);
//        if (mStreamController != null) {
//            mStreamController.stop();
//        }
//        if (tcpSender != null) {
//            tcpSender.stop();
//        }
//    }


    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopRecording();
        return true;


    }

    private void stopRecording() {
        Log.e("ttt", "stopRecording: zzz");
        if (mStreamController != null) {
            mStreamController.stop();
        }
        shutDown();
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        ToastUtils.show(this, "投屏服务停止了", Toast.LENGTH_SHORT);
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }

}