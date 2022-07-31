package com.example.hikerview.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.hikerview.R;
import com.example.hikerview.ui.Application;
import com.example.hikerview.ui.browser.WebViewActivity;
import com.example.hikerview.ui.video.EmptyActivity;

/**
 * 作者：By 15968
 * 日期：On 2021/8/18
 * 时间：At 11:22
 */

public class SearchAppWidget extends AppWidgetProvider {
    private static final String CLICK = "com.hiker.youtoo.ui.widget.SEARCH";

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (CLICK.equals(intent.getAction())) {
            if (Application.hasMainActivity()) {
//                EventBus.getDefault().post(new OnUrlChangeEvent("hiker://search"));
                Intent intent1 = new Intent(context, EmptyActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            } else {
                Intent intent1 = new Intent(context, WebViewActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("url", "hiker://search");
                context.startActivity(intent1);
            }
        }
    }

    /**
     * 每次窗口小部件被更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Log.i("AppWidget", "开始了更新");
        update(context, appWidgetManager);
    }

    public static void update(Context context) {
        update(context, null);
    }

    public static void update(Context context, AppWidgetManager appWidgetManager) {
        new Thread(() -> {
            try {
                RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_search);
                Intent clickIntent = new Intent(CLICK);
                clickIntent.setClass(context, SearchAppWidget.class);
                clickIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0);
                rv.setOnClickPendingIntent(R.id.bg, pendingIntent);

                //这里获得当前的包名，并且用AppWidgetManager来向NewAppWidget.class发送广播。
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                ComponentName cn = new ComponentName(context, SearchAppWidget.class);
                manager.updateAppWidget(cn, rv);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //context.stopService(new Intent(context, WidgetService.class));
        Log.i("AppWidget", "删除成功！");
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // Intent mTimerIntent = new Intent(context, WidgetService.class);
        // context.startService(mTimerIntent);
        Log.i("AppWidget", "创建成功！");
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        //  Intent mTimerIntent = new Intent(context, WidgetService.class);
        // context.stopService(mTimerIntent);
        Log.i("AppWidget", "删除成功！");
    }

    /**
     * 当小部件大小改变时
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }
}