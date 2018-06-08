package com.test.screenimage.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.test.screenimage.R;
import com.test.screenimage.core.BaseActivity;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ScanQrCodeActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.fl_my_container)
    FrameLayout flMyContainer;
    @BindView(R.id.btn_open)
    Button btnOpen;
    private Context mContext;
    private int REQUEST_CODE = 1;
    private CaptureFragment captureFragment;
    private boolean isOpen = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan_qr_code;
    }

    @Override
    protected void initView() {
        mContext = this;
        captureFragment = new CaptureFragment();
        // 为二维码扫描界面设置定制化界面
        CodeUtils.setFragmentArgs(captureFragment, R.layout.custom_camera);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container, captureFragment).commit();

    }

    @Override
    protected void initData() {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.btn_open})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_open:
                if (!isOpen) {
                    CodeUtils.isLightEnable(true);
                    isOpen = true;
                    return;
                }
                CodeUtils.isLightEnable(false);
                isOpen = true;
                break;

        }
    }

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_SUCCESS);
            bundle.putString(CodeUtils.RESULT_STRING, result);
            resultIntent.putExtras(bundle);
            ScanQrCodeActivity.this.setResult(RESULT_OK, resultIntent);
            ScanQrCodeActivity.this.finish();
        }

        @Override
        public void onAnalyzeFailed() {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(CodeUtils.RESULT_TYPE, CodeUtils.RESULT_FAILED);
            bundle.putString(CodeUtils.RESULT_STRING, "");
            resultIntent.putExtras(bundle);
            ScanQrCodeActivity.this.setResult(RESULT_OK, resultIntent);
            ScanQrCodeActivity.this.finish();
        }
    };


}
