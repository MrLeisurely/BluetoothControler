package com.example.bloothcontroler.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by HanWenHao on 2017/5/16.
 */

public class ScreenUtil {
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }
    public static int getStatusBarHeight(Context context){
        double statusBarHeight = Math.ceil(25 * context.getResources().getDisplayMetrics().density);
        return (int) statusBarHeight;
    }

    /**
     * 是否有虚拟导航栏
     * @param activity
     * @return
     */

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean phoneHasNav(Activity activity){
        boolean flag = false;

        if(Build.VERSION.SDK_INT < 14){
            flag = false;
        }else {
            View content = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            if (content != null) {
                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point point = new Point();
                display.getRealSize(point);

                int right = content.getRight();
                if (right != point.y) {
                    flag = true;
                }
            }
        }
        return flag;
    }
}
