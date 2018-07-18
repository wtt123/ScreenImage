package com.test.screenimage.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.skydoves.elasticviews.ElasticButton;
import com.test.screenimage.R;
import com.test.screenimage.configuration.AudioConfiguration;
import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.constant.Constants;
import com.test.screenimage.constant.ScreenImageApi;
import com.test.screenimage.controller.StreamController;
import com.test.screenimage.controller.audio.NormalAudioController;
import com.test.screenimage.controller.video.ScreenVideoController;
import com.test.screenimage.core.BaseActivity;
import com.test.screenimage.net.OnTcpSendMessageListner;
import com.test.screenimage.net.TcpUtil;
import com.test.screenimage.net.boastcast.NetWorkStateReceiver;
import com.test.screenimage.stream.packer.TcpPacker;
import com.test.screenimage.stream.sender.OnSenderListener;
import com.test.screenimage.stream.sender.tcp.TcpSender;
import com.test.screenimage.stream.sender.udp.UDPClientThread;
import com.test.screenimage.stream.sender.udp.interf.OnUdpConnectListener;
import com.test.screenimage.utils.DialogUtils;
import com.test.screenimage.utils.NetWorkUtils;
import com.test.screenimage.utils.PreferenceUtils;
import com.test.screenimage.utils.SopCastLog;
import com.test.screenimage.utils.SopCastUtils;
import com.test.screenimage.utils.ToastUtils;
import com.test.screenimage.widget.CustomDialog;
import com.test.screenimage.widget.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.Socket;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by wt on 2018/6/4.
 */
public class ScreenImageActivity extends BaseActivity implements View.OnClickListener,
        OnSenderListener, OnUdpConnectListener {

    @BindView(R.id.btn_start)
    ElasticButton btnStart;
    @BindView(R.id.btn_stop)
    ElasticButton btnStop;
    private String TAG = "ScreenImageActivity";
    private MediaProjectionManager mMediaProjectionManage;
    private static final int RECORD_REQUEST_CODE = 101;
    private StreamController mStreamController;
    private VideoConfiguration mVideoConfiguration;
    private TcpSender tcpSender;

    private int port = 11111;
    private String mIp;
    //是否已经开启投屏了
    private boolean isStart = false;
    private boolean isNetBad = false;
    private boolean isNetConnet = false;
    private boolean isDisconnect = true;
    private Context context;
    private LoadingDialog loadingDialog;
    private CustomDialog customDialog;
    private TcpUtil mTcpUtil;
    private int mCurrentBps;
    private int netBodCount = 0;
    private UDPClientThread clientThread;
    private ProgressDialog progressDialog;
    private NetWorkStateReceiver netWorkStateReceiver;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_screen_image;
    }

    @Override
    protected void initView() {
        context = this;
    }

    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
        if (NetWorkUtils.isNetWorkConnected(context)) {
            showProgress();
            clientThread = new UDPClientThread(this);
            return;
        }
    }

    // TODO: 2018/7/11 显示dialog
    private void showProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("系统提示");
        progressDialog.setMessage("正在连接您的显示屏,请稍后...");
        progressDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.btn_start, R.id.btn_stop})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (!NetWorkUtils.isNetWorkConnected(context)) {
                    ToastUtils.showShort(context, "暂不能投屏，请检查网络");
                    return;
                }
                if (isStart) {
                    ToastUtils.showShort(context, "正在投屏中，再次点击无效");
                    return;
                }
                mTcpUtil.sendMessage(ScreenImageApi.LOGIC_REQUEST.MAIN_CMD,
                        ScreenImageApi.LOGIC_REQUEST.GET_START_INFO
                        , Constants.WIDTH + "," + Constants.HEIGHT, new OnTcpSendMessageListner() {
                            @Override
                            public void success(int mainCmd, int subCmd, String body, byte[] bytes) {
                                if (mainCmd != ScreenImageApi.LOGIC_REPONSE.MAIN_CMD ||
                                        subCmd != ScreenImageApi.LOGIC_REPONSE.GET_START_INFO) {
                                    Log.e(TAG, "收到指令不正确");
                                    return;
                                }
                                Log.e(TAG, "tcp 请求成功 内容是" + body);
                                //开始录制视频
                                requestRecording();
                            }

                            @Override
                            public void error(Exception e) {
                                ToastUtils.showShort(context, e.getMessage());
                            }
                        });


                break;
            case R.id.btn_stop:
                stopRecording();
                isStart = false;
                break;
        }
    }


    /**
     * 开始录屏
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestRecording() {
        if (!SopCastUtils.isOverLOLLIPOP()) {
            ToastUtils.showShort(context, "此设备不支持录制屏幕");
            return;
        }
        mMediaProjectionManage = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mMediaProjectionManage.createScreenCaptureIntent();
        startActivityForResult(captureIntent, RECORD_REQUEST_CODE);

    }


    /**
     * 跳转回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                NormalAudioController audioController = new NormalAudioController(ScreenImageActivity.this);
                ScreenVideoController videoController = new ScreenVideoController(mMediaProjectionManage, resultCode, data);
                mStreamController = new StreamController(videoController, audioController);
                requestRecordSuccess();
            } else {
                requestRecordFail();
            }
        }
    }

    /**
     * 允许录制
     */
    private void requestRecordSuccess() {
        startRecord();
    }

    private void requestRecordFail() {
        Log.e(TAG, "requestRecordFail: 用戶拒绝录制屏幕");
    }


    /**
     * 连接/编码/发送
     */
    private void startRecord() {
        TcpPacker packer = new TcpPacker();
        packer.setSendAudio(true);
        packer.initAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
        mVideoConfiguration = new VideoConfiguration.Builder().build();
        setVideoConfiguration(mVideoConfiguration);
        setRecordPacker(packer);

        tcpSender = new TcpSender(mIp, port);
        tcpSender.setMianCmd(ScreenImageApi.RECORD.MAIN_CMD);
        tcpSender.setSubCmd(ScreenImageApi.RECORD.RECORDER_REQUEST_START);
        tcpSender.setSendBody(Build.MODEL);
        tcpSender.setVideoParams(mVideoConfiguration);
        tcpSender.setSenderListener(this);
        //创建连接
        tcpSender.openConnect();
        setRecordSender(tcpSender);
        //开始执行
        startRecording();
    }


    private void setVideoConfiguration(VideoConfiguration videoConfiguration) {
        if (mStreamController != null) {
            mStreamController.setVideoConfiguration(videoConfiguration);
        }
    }

    /**
     * 编码
     *
     * @param packer
     */
    private void setRecordPacker(TcpPacker packer) {
        if (mStreamController != null) {
            mStreamController.setPacker(packer);
        }
    }

    /**
     * 设置发送器
     *
     * @param tcpSender
     */
    private void setRecordSender(TcpSender tcpSender) {
        if (mStreamController != null) {
            mStreamController.setSender(tcpSender);
        }
    }

    private void startRecording() {
        if (mStreamController != null) {
            //开始录制等操作
            mStreamController.start();
        }
    }


    /**
     * 停止录屏
     */
    private void stopRecording() {
        if (mTcpUtil != null) {
            mTcpUtil.cancel();
        }
        if (mStreamController != null) {
            mStreamController.stop();
            isStart = false;
            isNetBad = true;
            ToastUtils.showShort(context, "已停止投屏");
        }
    }

    // TODO: 2018/7/2 网络切换时更新当前ui
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(boolean state) {
        if (!state) {
            stopRecording();
        }
    }

    @Override
    public void onConnecting() {
        Log.e(TAG, "onConnecting: 链接中");
    }

    @Override
    public void onConnected() {
        Log.e("wtt", "onConnected: zz");
        //连接成功
        mCurrentBps = mVideoConfiguration.maxBps;
        isStart = true;
        isDisconnect = true;
        if (loadingDialog == null) return;
        loadingDialog.dismiss();
        if (customDialog == null) return;
        customDialog.dismiss();
    }

    @Override
    public void onDisConnected(String message) {
        //连接断开
        isStart = false;
        isNetConnet = false;
        if (isDisconnect) {
            new CustomDialog(context).builder()
                    .setTitle("温馨提示！")
                    .setMessage("连接意外断开")
                    .setPositiveButton("停止投屏", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopRecording();
                            isDisconnect = false;
                        }
                    })
                    .setNegativeButton("继续投屏", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startRecord();
                        }
                    })
                    .setCancelable(false).show();
        }
        Log.e(TAG, "onConnected: 连接失败");
    }

    @Override
    public void onConnectFail(String message) {
        //连接失败
        isNetConnet = false;
        isStart = false;
        ToastUtils.showShort(context, "投屏失败，请重新投屏！");
    }

    @Override
    public void onPublishFail() {
        //发送失败
        Log.e(TAG, "onConnected: 发送失败");
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

        isNetBad = false;
        Log.e(TAG, "onConnected: 网络好");
        if (loadingDialog == null) return;
        loadingDialog.dismiss();
        if (customDialog == null) return;
        customDialog.dismiss();
    }

    @Override
    public void onNetBad() {
        Log.e(TAG, "onConnected: 网络差");
        if (mCurrentBps == mVideoConfiguration.minBps) {
            netBodCount++;
            if (netBodCount >= 2) {
                netBodCount = 0;
//                createNotification();
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


        isNetBad = true;
        //网络差
        if (isNetBad && isNetConnet) {
            customDialog = new CustomDialog(context);
            customDialog.builder()
                    .setTitle("温馨提示！")
                    .setMessage("当前网络较差")
                    .setPositiveButton("停止投屏", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopRecording();
                        }
                    })
                    .setNegativeButton("继续投屏", new View.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onClick(View v) {
                            isStart = false;
                        }
                    })
                    .setCancelable(false).show();
            isNetBad = false;
        }

    }

    @Override
    protected void onResume() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        Log.e("lw", "onResume: zzz");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkStateReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (netWorkStateReceiver != null) {
            unregisterReceiver(netWorkStateReceiver);
        }
        if (tcpSender != null) {
            tcpSender.stop();
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void udpConnectSuccess(String ip) {
        //udp连接成功
        progressDialog.dismiss();
        clientThread.interrupt();
        ToastUtils.showShort(context, "连接成功");
        if (!TextUtils.isEmpty(ip)) {
            mIp = ip;
            mTcpUtil = new TcpUtil(mIp, port);
        }
    }

    @Override
    public void udpDisConnec(String message) {
    }
}
