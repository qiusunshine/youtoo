package com.example.hikerview.utils;

import android.app.Activity;
import android.os.Build;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.githang.statusbar.StatusBarCompat;

/**
 * 作者：By 15968
 * 日期：On 2020/6/28
 * 时间：At 20:05
 */
public class StatusBarCompatUtil {
//    public static void setNowColor(int nowColor) {
//        StatusBarCompatUtil.nowColor = nowColor;
//    }
//
//    private static int nowColor;

    public static void setStatusBarColor(Activity activity, int color) {
//        if (nowColor == color) {
//            return;
//        }
//        StatusBarCompat.setStatusBarColor(activity, color);
//        nowColor = color;
        setStatusBarColorForce(activity, color);
    }

    public static void setStatusBarColorForce(Activity activity, int color) {
        StatusBarCompat.setStatusBarColor(activity, color);
        if (Build.VERSION.SDK_INT >= 30 && PreferenceMgr.getBoolean(activity, "home_logo_dark", false)) {
            boolean isLightColor = StatusBarCompat.toGrey(color) > 225;
            WindowInsetsControllerCompat wic = ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
            if (wic != null) {
                // true表示Light Mode，状态栏字体呈黑色，反之呈白色
                wic.setAppearanceLightStatusBars(isLightColor);
            }
        }
//        nowColor = color;
    }
}
