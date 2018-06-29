package com.test.screenimage.core;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.test.screenimage.utils.StatusBarUtil;
import com.test.screenimage.utils.SupportMultipleScreensUtil;
import com.test.screenimage.utils.ToastUtils;

import butterknife.ButterKnife;


/**
 * Created by wt on 2018/5/28.
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static long lastTimeStamp = 0l;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayoutId() != 0) {
            StatusBarUtil.transparencyBar(this);
            StatusBarUtil.StatusBarLightMode(this);
            setContentView(getLayoutId());
            View rootView = findViewById(android.R.id.content);
            SupportMultipleScreensUtil.scale(rootView);
            ButterKnife.bind(this);
        }
        initView();
        initData();
    }

    protected abstract void initView();

    protected abstract void initData();

    protected int getLayoutId() {
        return 0;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 退出程序.
     *
     * @param context
     */
    public static void exitApplication(Activity context) {
        long currentTimeStamp = System.currentTimeMillis();
        if (currentTimeStamp - lastTimeStamp > 1350L) {
            ToastUtils.showToast(context, "再按一次退出");
        } else {
            context.finish();
        }
        lastTimeStamp = currentTimeStamp;
    }

}
