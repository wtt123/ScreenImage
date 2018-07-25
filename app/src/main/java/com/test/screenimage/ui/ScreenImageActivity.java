package com.test.screenimage.ui;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.os.Build;

import android.os.IBinder;
import android.support.annotation.RequiresApi;



import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.test.screenimage.R;


import com.test.screenimage.constant.Constants;
import com.test.screenimage.constant.ScreenImageApi;



import com.test.screenimage.core.BaseActivity;
import com.test.screenimage.net.OnTcpSendMessageListner;
import com.test.screenimage.net.TcpUtil;
import com.test.screenimage.net.boastcast.NetWorkStateReceiver;
import com.test.screenimage.service.ScreenImageService;

import com.test.screenimage.stream.sender.OnSenderListener;

import com.test.screenimage.stream.sender.udp.UDPClientThread;
import com.test.screenimage.stream.sender.udp.interf.OnUdpConnectListener;
import com.test.screenimage.utils.BatteryUtils;

import com.test.screenimage.utils.NetWorkUtils;


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
 * OnSenderListener
 */
public class ScreenImageActivity extends BaseActivity implements View.OnClickListener,
        OnUdpConnectListener, OnSenderListener {

    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_stop)
    Button btnStop;
    private String TAG = "ScreenImageActivity";

    private static final int RECORD_REQUEST_CODE = 101;

    private int port = 11111;
    private String mIp = "192.168.1.115";
    //是否已经开启投屏了
    private boolean isStart = false;
    private boolean isNetBad = false;
    private boolean isNetConnet = false;
    private boolean isDisconnect = true;
    private Context context;
    private LoadingDialog loadingDialog;
    private CustomDialog customDialog;
    private TcpUtil mTcpUtil;

    private UDPClientThread clientThread;
    private ProgressDialog progressDialog;
    private NetWorkStateReceiver netWorkStateReceiver;

    private int mResultCode;
    private MyServiceConnect mConnect;
    private Intent mData;
    private MediaProjectionManager mMediaProjectionManage;

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
                if (mTcpUtil == null) mTcpUtil = new TcpUtil(mIp, port);
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
                mResultCode = resultCode;
                mData = data;
                startRecord();
            } else {
                Log.e(TAG, "requestRecordFail: 用戶拒绝录制屏幕");
            }
        }
    }

    private void startRecord() {
        Intent intent = new Intent(this, ScreenImageService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        mConnect = new MyServiceConnect();
        bindService(intent, mConnect, Context.BIND_AUTO_CREATE);
        checkIgnoreBattery();
    }

    //检查是否忽略电池优化
    private void checkIgnoreBattery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                BatteryUtils.ignoreBatteryOptimization(this);
            } catch (Exception e) {
                Log.e(TAG, "unable to set ignore battery optimization Exception = " + e.toString());
            }
        }
    }

    class MyServiceConnect implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ((ScreenImageService.ScreenImageBinder) binder).getService().
                    startController(mMediaProjectionManage, mResultCode, mData,
                            ScreenImageActivity.this, mIp, port);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


    /**
     * 停止录屏
     */
    private void stopRecording() {
        if (mTcpUtil != null) {
            mTcpUtil.cancel();
        }
        try {
            unbindService(mConnect);
        } catch (Exception e) {

        }
        isStart = false;
        isNetBad = true;
        ToastUtils.showShort(context, "已停止投屏");

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
    public void shutDown() {
        Log.e("wtt", "shutDown: zzz" );
        if (mTcpUtil!=null)mTcpUtil.cancel();
    }

    @Override
    public void netSpeedChange(String netSpeedMsg) {
        Log.e(TAG,"" + netSpeedMsg);
    }

    @Override
    protected void onResume() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
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
