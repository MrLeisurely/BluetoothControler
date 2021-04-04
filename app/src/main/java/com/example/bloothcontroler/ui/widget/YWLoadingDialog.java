package com.example.bloothcontroler.ui.widget;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bloothcontroler.R;


/**
 * 加载dialog
 *
 * @author Driver
 * @version V1.0
 * @Date 2015-04-01
 */

public class YWLoadingDialog extends Dialog {
    private ImageView iv_load_result;// 加载的结果图标显示
    private TextView tv_load;// 加载的文字展示
    private ProgressBar pb_loading;// 加载中的图片
    private final int LOAD_SUCC = 0x001;
    private final int LOAD_FAIL = 0x002;
    private String values = "";
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LOAD_SUCC:
                    dismiss();
                    break;
                case LOAD_FAIL:
                    dismiss();
                    break;
                default:
                    break;
            }
        }
    };

    public YWLoadingDialog(Context context, String values) {
        super(context, R.style.myDialogTheme2);
        this.values = values;
    }

    public YWLoadingDialog(Context context) {
        super(context, R.style.myDialogTheme2);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_loading_layout);
        iv_load_result = (ImageView) findViewById(R.id.iv_load_result);
        tv_load = (TextView) findViewById(R.id.tv_load);
        pb_loading = (ProgressBar) findViewById(R.id.pb_loading);
        if (null != values && !"".equals(values))
            tv_load.setText(values);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void dismiss() {
        if (isShowing()){
            tv_load.setText("");
            super.dismiss();
        }
    }

    public void showWithMsg(String msg){
        if (!isShowing()){
            show();
            updateValue(msg);
        }
    }

    public void updateValue(String values){
        if (null != tv_load && null != values){
            tv_load.setVisibility(View.VISIBLE);
            tv_load.setText(values);
        }
    }
}

