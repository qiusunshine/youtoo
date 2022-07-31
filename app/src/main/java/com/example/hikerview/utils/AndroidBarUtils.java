package com.example.hikerview.utils;

/**
 * 作者：By 15968
 * 日期：On 2021/7/14
 * 时间：At 15:45
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.hikerview.R;
import com.example.hikerview.ui.browser.WebViewActivity;

import java.lang.reflect.Field;

import static com.example.hikerview.ui.browser.WebViewActivity.KEY_FULL_THEME;

/**
 * @author zsl
 * @date 2018/6/13
 * @description StatusBar 和 NavigationBar 的工具类
 */
public class AndroidBarUtils {

    private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
    private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
    private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
    public static int mainFlag = -10000;
    private static int defPadding;

    /**
     * 判断当前环境是否 Dark Mode
     */
    public static boolean isDarkMode(Context context) {
        return (context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
    /**
     * 设置透明StatusBar,默认文字为白色
     *
     * @param activity Activity
     */
    public static void setTranslucent(Activity activity) {
        setTranslucentStatusBar(activity, true);
    }

    /**
     * 设置 DrawerLayout 在4.4版本下透明，不然会出现白边
     *
     * @param drawerLayout DrawerLayout
     */
    public static void setTranslucentDrawerLayout(DrawerLayout drawerLayout) {
        if (drawerLayout != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawerLayout.setFitsSystemWindows(true);
            drawerLayout.setClipToPadding(false);
        }
    }

    /**
     * 设置透明StatusBar
     *
     * @param activity Activity
     */
    public static void setTranslucentStatusBar(Activity activity, boolean trans) {
        if (activity instanceof WebViewActivity) {
            boolean fullTheme = PreferenceMgr.getBoolean(activity, KEY_FULL_THEME, false);
            if (fullTheme) {
                return;
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        Window window = activity.getWindow();
        //透明状态栏
//        if (trans) {
//            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        } else {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (trans) {
                if (-10000 == mainFlag) {
                    mainFlag = window.getDecorView().getSystemUiVisibility();
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.setStatusBarColor(Color.TRANSPARENT);
            } else {
                window.getDecorView().setSystemUiVisibility(mainFlag);
                StatusBarCompatUtil.setStatusBarColor(activity, activity.getResources().getColor(R.color.white));
            }
            //5.0及以上版本
//            createNavBar(activity);
        } else {
            //4.4版本
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    /**
     * 设置透明StatusBar
     *
     * @param activity Activity
     */
    public static void setTranslucentStatusBar2(Activity activity, boolean isInit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        Window window = activity.getWindow();
        //透明状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int uiConfig = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            if (isInit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isDarkMode(activity)) {
                        uiConfig = uiConfig & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    } else {
                        uiConfig = uiConfig | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                    }
                }
            }
            window.getDecorView().setSystemUiVisibility(uiConfig);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            //5.0及以上版本
//            createNavBar(activity);
        } else {
            //4.4版本
            // TODO 这里不知道要不要处理，没有 4.4 的测试机，遇到问题在解开看看吧
            // window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Android 6.0使用原始的主题适配
     *
     * @param activity Activity
     * @param darkMode 是否是黑色模式
     */
    public static void setBarDarkMode(Activity activity, boolean darkMode) {
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        //设置StatusBar模式
        if (darkMode) {//黑色模式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//设置statusBar和navigationBar为黑色
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {//设置statusBar为黑色
                    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        } else {//白色模式
            int statusBarFlag = View.SYSTEM_UI_FLAG_VISIBLE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                statusBarFlag = window.getDecorView().getSystemUiVisibility()
                        & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//设置statusBar为白色，navigationBar为灰色
//                int navBarFlag = window.getDecorView().getSystemUiVisibility()
//                        & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;//如果想让navigationBar为白色，那么就使用这个代码。
                int navBarFlag = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(navBarFlag | statusBarFlag);
            } else {
                window.getDecorView().setSystemUiVisibility(statusBarFlag);
            }
        }
        setHuaWeiStatusBar(darkMode, window);
    }

    /**
     * 设置华为手机 StatusBar
     *
     * @param darkMode 是否是黑色模式
     * @param window   window
     */
    private static void setHuaWeiStatusBar(boolean darkMode, Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
                Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
                field.setAccessible(darkMode);
                field.setInt(window.getDecorView(), Color.TRANSPARENT);  //改为透明
            } catch (ClassNotFoundException e) {
                Log.e("setHuaWeiStatusBar", "HuaWei status bar 模式设置失败");
            } catch (IllegalAccessException e) {
                Log.e("setHuaWeiStatusBar", "HuaWei status bar 模式设置失败");
            } catch (NoSuchFieldException e) {
                Log.e("setHuaWeiStatusBar", "HuaWei status bar 模式设置失败");
            }
        }
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Activity context) {
        // 获得状态栏高度
        return MyStatusBarUtil.getStatusBarHeight(context);
    }

    /**
     * 获取Bar高度
     *
     * @param context context
     * @param barName 名称
     * @return Bar高度
     */
    public static int getBarHeight(Context context, String barName) {
        // 获得状态栏高度
        int resourceId = context.getResources().getIdentifier(barName, "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    public static void isNavigationBarExist(@NonNull Activity activity, final OnNavigationStateListener onNavigationStateListener) {
        final int height = getBarHeight(activity, NAV_BAR_WIDTH_RES_NAME);
        activity.getWindow().getDecorView().setOnApplyWindowInsetsListener((v, windowInsets) -> {
            boolean isShowing = false;
            int b = 0;
            if (windowInsets != null) {
                b = windowInsets.getSystemWindowInsetBottom();
                isShowing = (b == height);
            }
            if (onNavigationStateListener != null && b <= height) {
                onNavigationStateListener.onNavigationState(isShowing, b);
            }
            return windowInsets;
        });
    }

    public interface OnNavigationStateListener {
        void onNavigationState(boolean isShow, int height);
    }

    /**
     * 设置BarPaddingTop
     *
     * @param context Activity
     * @param view    View[ToolBar、TitleBar、navigationView.getHeaderView(0)]
     */
    public static void setBarPaddingTop(Activity context, View view) {
        int paddingStart = view.getPaddingStart();
        int paddingEnd = view.getPaddingEnd();
        int paddingBottom = view.getPaddingBottom();
        int statusBarHeight = getStatusBarHeight(context);
        //改变titleBar的高度
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height += statusBarHeight;
        view.setLayoutParams(lp);
        //设置paddingTop
        view.setPaddingRelative(paddingStart, statusBarHeight, paddingEnd, paddingBottom);
    }

    /**
     * 设置BarPaddingTop
     *
     * @param context Activity
     * @param view    View[ToolBar、TitleBar、navigationView.getHeaderView(0)]
     */
    public static void setBarPaddingTopForFrameLayout(Activity context, View view) {
        int statusBarHeight = getStatusBarHeight(context);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin + statusBarHeight, layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }
}