package com.example.hikerview.utils;

import android.content.Context;

/**
 * 作者：By 15968
 * 日期：On 2019/10/31
 * 时间：At 20:26
 */
public class SplashUtil {
    private static boolean hasCheck = false;
    private static int splashOpenTimes = -1;
    private static int shouldTimes = 2;

    public static void setShowLoading(boolean showLoading) {
        SplashUtil.showLoading = showLoading;
    }

    private static boolean showLoading = true;


    public static int getSplashOpenTimes(Context context) {
        if (splashOpenTimes >= 0) {
            return splashOpenTimes;
        } else {
            splashOpenTimes = PreferenceMgr.getInt(context, "splashOpenTimes", 0);
            return splashOpenTimes;
        }
    }

    public static void setSplashOpenTimes(Context context, int shouldTimes) {
        PreferenceMgr.put(context, "splashOpenTimes", shouldTimes);
        splashOpenTimes = shouldTimes;
    }

    public static boolean canShowDialog(Context context) {
        return showLoading && getSplashOpenTimes(context) >= shouldTimes;
    }
}
