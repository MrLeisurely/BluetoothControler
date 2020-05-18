package com.example.bloothcontroler.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.bloothcontroler.R;


/**
 * Created by HanWenHao on 2018/10/18.
 */

public class FontTabItem extends LinearLayout {
    private Context mContext;
    private TextView cu;
    private boolean itemOk;
    private boolean isChecked = false;
    public FontTabItem(Context context) {
        super(context);
        init(context,null);
    }
    public FontTabItem(Context context, String titleres) {
        super(context);
        init(context,null);
        setTitle(titleres);
    }
    public FontTabItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public FontTabItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FontTabItem(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs);
    }
    private void init(Context context, @Nullable AttributeSet attrs){
        mContext = context;
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.tab_item, this);
        cu = view.findViewById(R.id.tv_tab);

    }
    public void setTitle(String title){
        if (null!=cu)
            cu.setText(title);
    }

    public void setItemOk(boolean itemOk) {
        this.itemOk = itemOk;
        if (!isChecked)
        unCheck();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public FontTabItem checked(){
        if (null!=cu){
            cu.setTextColor(ContextCompat.getColor(mContext,R.color.base_blue));
            cu.setBackground(ContextCompat.getDrawable(mContext,R.drawable.btn_bg_white));
        }
        isChecked = true;
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public FontTabItem unCheck(){
        if (null!=cu){
//            if (itemOk){
//                cu.setTextColor(ContextCompat.getColor(mContext,R.color.background_red));
//            }
//            else {
                cu.setTextColor(ContextCompat.getColor(mContext,R.color.base_white));
//            }
            cu.setBackground(ContextCompat.getDrawable(mContext,R.drawable.app_shape_btn_bg));
        }
        isChecked = false;
        return this;
    }
}
