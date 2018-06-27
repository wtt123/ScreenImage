package com.test.screenimage.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.test.screenimage.R;
import com.test.screenimage.configuration.AudioConfiguration;
import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.constant.ScreenImageApi;
import com.test.screenimage.controller.StreamController;
import com.test.screenimage.controller.audio.NormalAudioController;
import com.test.screenimage.controller.video.ScreenVideoController;
import com.test.screenimage.core.BaseActivity;
import com.test.screenimage.stream.packer.TcpPacker;
import com.test.screenimage.stream.sender.OnSenderListener;
import com.test.screenimage.stream.sender.tcp.TcpSender;
import com.test.screenimage.utils.DialogUtils;
import com.test.screenimage.utils.SopCastLog;
import com.test.screenimage.utils.SopCastUtils;
import com.test.screenimage.utils.ToastUtils;
import com.test.screenimage.widget.CustomDialog;
import com.test.screenimage.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by wt on 2018/6/4.
 */
public class ScreenImageActivity extends BaseActivity implements View.OnClickListener, OnSenderListener {

    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    private String TAG = "wt";
    private MediaProjectionManager mMediaProjectionManage;
    private static final int RECORD_REQUEST_CODE = 101;
    private StreamController mStreamController;
    private VideoConfiguration mVideoConfiguration;
    private TcpSender tcpSender;

    private int port = 11111;
    private String mIp;
    //是否已经开启投屏了
    private boolean isStart = false;
    private boolean isNetBad = true;
    private Context context;
    private LoadingDialog loadingDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_screen_image;
    }

    @Override
    protected void initView() {
        context = this;
//        Intent intent = getIntent();
//        mIp = intent.getStringExtra("ip");
    }

    @Override
    protected void initData() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.btn_start, R.id.btn_stop})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                //开始录制视频
                if (isStart) {
                    ToastUtils.showShort(context,"正在投屏中，再次点击无效");
                    return;
                }
                requestRecording();
                isStart = true;
                break;
            case R.id.btn_stop:
                stopRecording();
                resetStatus();
                break;
        }
    }


    /**
     * 开始录屏
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void requestRecording() {
        if (!SopCastUtils.isOverLOLLIPOP()) {
            SopCastLog.d(TAG, "此设备不支持录制屏幕");
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

        tcpSender = new TcpSender("192.169.0.245", port);
        tcpSender.setMianCmd(ScreenImageApi.RECORD.MAIN_CMD);
        tcpSender.setSubCmd(ScreenImageApi.RECORD.RECORDER_REQUEST_START);
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
        if (mStreamController != null) {
            mStreamController.stop();
            resetStatus();
            isNetBad=true;
            ToastUtils.showShort(context,"已停止投屏");
        }
    }

    @Override
    public void onConnecting() {
        Log.e(TAG, "onConnecting: 链接中" );
    }

    @Override
    public void onConnected() {
        //连接成功
        Log.e(TAG, "onConnected: 连接成功");
        if (loadingDialog==null){
            return;
        }
        loadingDialog.dismiss();
    }

    @Override
    public void onDisConnected() {
        //连接失败
        ToastUtils.showShort(context,"连接已断开");
        resetStatus();
        Log.e(TAG, "onConnected: 连接失败");
    }

    @Override
    public void onPublishFail() {
        //发送失败
        Log.e(TAG, "onConnected: 发送失败");
    }


    @Override
    public void onNetGood() {
        //网络好
        Log.e(TAG, "onConnected: 网络好");
        if (loadingDialog==null){
            return;
        }
        loadingDialog.dismiss();
    }

    @Override
    public void onNetBad() {
        Log.e(TAG, "onConnected: 网络差");
        //网络差
        if (isNetBad) {
            new CustomDialog(context).builder()
                    .setTitle("温馨提示！")
                    .setMessage("当前网络较差")
                    .setPositiveButton("停止投屏", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            stopRecording();
                        }
                    })
                    .setNegativeButton("继续投屏", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadingDialog = new LoadingDialog(context);
                            loadingDialog.show();

                        }
                    })
                    .setCancelable(false).show();
            isNetBad = false;
        }

    }

    // TODO: 2018/6/27 把开始投屏按钮状态重置
    private void resetStatus(){
        isStart=false;
    }

    @Override
    protected void onDestroy() {
        tcpSender.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

}
