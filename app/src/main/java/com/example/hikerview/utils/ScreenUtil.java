package com.example.hikerview.utils;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.lang.reflect.Method;

/**
 * 作者：By hdy
 * 日期：On 2019/3/23
 * 时间：At 0:03
 */
public class ScreenUtil {


    public static int getScreenHeight(Activity activity) {
        WindowManager manager = (activity).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenHeight = outMetrics.heightPixels;
        if (!isOrientation(activity) && outMetrics.widthPixels > screenHeight) {
            screenHeight = outMetrics.widthPixels;
        }
        return screenHeight;
    }

    public static int getScreenHeight2(Activity activity) {
        WindowManager manager = (activity).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    public static int getScreenWidth(Activity activity) {
        WindowManager manager = (activity).getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        if (!isOrientation(activity) && outMetrics.heightPixels < screenWidth) {
            screenWidth = outMetrics.heightPixels;
        }
        return screenWidth;
    }


    public static int getScreenMin(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    public static int getScreenMax(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return Math.max(outMetrics.widthPixels, outMetrics.heightPixels);
    }


    public static int getScreenWidth3(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获取整个手机屏幕的大小(包括虚拟按钮)
     * 必须在onWindowFocus方法之后使用
     *
     * @param activity
     * @return
     */
    public static int[] getScreenSize(Activity activity) {
        int[] size = new int[2];
        View decorView = activity.getWindow().getDecorView();
        size[0] = decorView.getWidth();
        size[1] = decorView.getHeight();
        return size;
    }

    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取虚拟按键的高度
     */
    public static int getNavigationBarHeight(Activity activity) {
        int navigationBarHeight = 0;
        Resources rs = activity.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && hasNavigationBar(activity)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    /**
     * 是否存在虚拟按键
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private static boolean hasNavigationBar(Activity activity) {
        boolean hasNavigationBar = false;
        Resources rs = activity.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            @SuppressLint("PrivateApi") Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception ignored) {
        }
        return hasNavigationBar;
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        return activity
                .getResources()
                .getDisplayMetrics();
    }

    public static int toPixels(Resources res, float dp) {
        return (int) (dp * res.getDisplayMetrics().density);
    }

    /**
     * Converts sp to px
     *
     * @param res Resources
     * @param sp  the value in sp
     * @return int
     */
    public static int toScreenPixels(Resources res, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.getDisplayMetrics());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }


    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
                && isPad(context);
    }

    public static boolean isPad(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        // 屏幕尺寸
        double screenInches = Math.sqrt(x + y);
        // 大于7尺寸则为Pad
        return screenInches >= 7.0;
    }

    public static boolean isOrientation(Context activity) {
        if (activity == null || activity.getResources() == null) {
            return false;
        }
        return activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int getAppHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return -1;
        } else {
            Point point = new Point();
            wm.getDefaultDisplay().getSize(point);
            return point.y;
        }
    }


    public static int getScreenHeight2(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return -1;
        } else {
            Point point = new Point();
            if (Build.VERSION.SDK_INT >= 17) {
                wm.getDefaultDisplay().getRealSize(point);
            } else {
                wm.getDefaultDisplay().getSize(point);
            }

            return point.y;
        }
    }

    public static void setStatusBarVisibility(Activity activity, int defaultSystemUiVisibility, boolean visible) {
        int i = Build.VERSION.SDK_INT;
        Window localWindow = activity.getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        if (!visible) {
//            if (i >= 28) {
//                localLayoutParams.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//                localWindow.setAttributes(localLayoutParams);
//            }
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            WindowInsetsControllerCompat controllerCompat = getWindowInsetsController(activity);
            if (controllerCompat != null) {
                controllerCompat.hide(WindowInsetsCompat.Type.systemBars());
                return;
            }
        } else {
//            if (i >= 28) {
//                localLayoutParams.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
//                localWindow.setAttributes(localLayoutParams);
//            }
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            WindowInsetsControllerCompat controllerCompat = getWindowInsetsController(activity);
            if (controllerCompat != null) {
                controllerCompat.show(WindowInsetsCompat.Type.systemBars());
                return;
            }
        }
        if (!visible) {
            localWindow.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            localWindow.getDecorView().setSystemUiVisibility(defaultSystemUiVisibility);
        }
    }

    private static WindowInsetsControllerCompat getWindowInsetsController(Activity activity) {
        if (Build.VERSION.SDK_INT >= 30) {
            //在部分SDK_INT < 30的系统上直接用getWindowInsetsController拿不到
            return ViewCompat.getWindowInsetsController(activity.getWindow().getDecorView());
        }
        return WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
    }

    public static void setDisplayInNotch(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Window window = activity.getWindow();
            // 延伸显示区域到耳朵区
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }
}
