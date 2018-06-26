package com.test.screenimage.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.Permission;
import com.test.screenimage.R;
import com.test.screenimage.core.BaseActivity;
import com.test.screenimage.utils.ToastUtils;

import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.btn_scan)
    Button btnScan;
    private Context mContext;
    private int REQUEST_CODE = 001;
    private Intent mIntent = new Intent();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        mContext = this;
    }

    @Override
    protected void initData() {
        RxPermissions rxPermissions = new RxPermissions(this);
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.CAMERA};
        rxPermissions
                .requestEach(permissions)
                .subscribe(permission -> { // will emit 2 Permission objects
                    if (permission.granted) {
                        Log.e("wtt", "accept: 同意");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        ToastUtils.showShort(mContext, "拒绝权限，等待下次询问哦");

                    } else {
                        startAppSettings();
                        ToastUtils.showShort(mContext, "拒绝权限，不再弹出询问框，请前往APP应用设置中打开此权限");
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.btn_scan})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                mIntent.setClass(mContext, ScanQrCodeActivity.class);
                startActivityForResult(mIntent, REQUEST_CODE);
                break;

        }
    }

    /**
     * 处理二维码扫描结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                switch (bundle.getInt(CodeUtils.RESULT_TYPE)) {
                    case CodeUtils.RESULT_SUCCESS:
                        String result = bundle.getString(CodeUtils.RESULT_STRING);
                        if (TextUtils.isEmpty(result)) {
                            ToastUtils.showShort(mContext, "数据为空，请重新扫码！");
                            return;
                        }
                        mIntent.setClass(mContext, ScreenImageActivity.class);
                        mIntent.putExtra("ip", result);
                        startActivity(mIntent);
                        finish();
                        break;
                    case CodeUtils.RESULT_FAILED:
                        ToastUtils.showShort(mContext, "解析二维码失败");
                        break;
                }
            }
        }
    }

    // TODO: 2018/6/25 去权限设置页
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
        startActivityForResult(intent, 100);
    }
}
