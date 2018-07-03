package com.test.screenimage.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
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
import com.test.screenimage.stream.packer.TcpPacker;
import com.test.screenimage.stream.sender.OnSenderListener;
import com.test.screenimage.stream.sender.tcp.TcpSender;
import com.test.screenimage.utils.DialogUtils;
import com.test.screenimage.utils.NetWorkUtils;
import com.test.screenimage.utils.PreferenceUtils;
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
    ElasticButton btnStart;
    @BindView(R.id.btn_stop)
    ElasticButton btnStop;
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
    private boolean isNetBad = false;
    private boolean isDisconnect = true;
    private Context context;
    private LoadingDialog loadingDialog;
    private CustomDialog customDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_screen_image;
    }

    @Override
    protected void initView() {
        context = this;
        Intent intent = getIntent();
        mIp = intent.getStringExtra("ip");
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
                    ToastUtils.showShort(context, "正在投屏中，再次点击无效");
                    return;
                }
                requestRecording();
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
        checkNet();
        if (!SopCastUtils.isOverLOLLIPOP()) {
            ToastUtils.showShort(context,"此设备不支持录制屏幕");
            return;
        }
        mMediaProjectionManage = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mMediaProjectionManage.createScreenCaptureIntent();
        startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
        isStart = true;
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
            isStart = false;
            isNetBad = true;
            ToastUtils.showShort(context, "已停止投屏");
        }
    }

    @Override
    public void onConnecting() {
        Log.e(TAG, "onConnecting: 链接中");
    }

    @Override
    public void onConnected() {
        //连接成功
        isNetBad = true;
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
        ToastUtils.showShort(context,"连接失败，请检查网络，重新扫码连接！！");
        PreferenceUtils.setString(context, Constants.PCIP, null);
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
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
        if (loadingDialog == null) return;
        loadingDialog.dismiss();
        if (customDialog == null) return;
        customDialog.dismiss();
    }

    @Override
    public void onNetBad() {
        Log.e(TAG, "onConnected: 网络差");
        //网络差
        if (isNetBad) {
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


    private void checkNet() {
        //网络检查
        if (!NetWorkUtils.isNetConnected(context) && !NetWorkUtils.isWifiActive(context)) {
            ToastUtils.showShort(context, "请先连接无线网");
            return;
        }
        if (!NetWorkUtils.isInChildNet(mIp, context)) {
            StringBuffer msg = new StringBuffer();
            msg.append("服务端端ip地址: " + Constants.PCIP).append(",").append("\n")
                    .append("客户端ip地址: " + Constants.PHONEIP).append(",").append("\n")
                    .append("子网掩码: ").append(Constants.MASK).append(",").append("\n")
                    .append("服务端和客户端可能不在同一个子网段").append("!").append("\n")
                    .append("请检查网络配置!").append("\n");
            String title = "建议将电脑和手机直接连至同一路由下!";
            SpannableString spanString = new SpannableString(msg + title);
            spanString.setSpan(new ForegroundColorSpan(Color.RED), msg.length(),
                    msg.length() + title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            new CustomDialog(context)
                    .builder()
                    .setTitle(title)
                    .setMessage(spanString)
                    .setNegativeButton("知道了", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        tcpSender.stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
