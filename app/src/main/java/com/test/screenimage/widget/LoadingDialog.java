package com.test.screenimage.widget;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.test.screenimage.R;
import com.test.screenimage.utils.SupportMultipleScreensUtil;

/**
 * 加载对话框控件
 *
 * @author wt
 * @version V1.0 创建时间：2017-3-7
 */
public class LoadingDialog extends Dialog {
    private LayoutParams lp;
    private LayoutInflater inflater;
    private Context mContext;

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    public LoadingDialog(Context context) {
        super(context, R.style.MyLoadDialog);
        this.mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.loadingdialog, null);
        SupportMultipleScreensUtil.scale(layout);
        setContentView(layout);
        // 设置window属性
        lp = getWindow().getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0; // 去背景遮盖
        lp.alpha = 1.0f;
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}