package com.example.hikerview.ui.view.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hikerview.R;
import com.example.hikerview.event.LoadingDismissEvent;
import com.example.hikerview.ui.download.DetectListener;
import com.example.hikerview.ui.download.DownloadManager;
import com.example.hikerview.ui.download.DownloadTask;
import com.example.hikerview.ui.download.VideoInfo;
import com.example.hikerview.ui.download.util.UUIDUtil;
import com.example.hikerview.ui.search.model.SearchHistroyModel;
import com.example.hikerview.ui.view.DownloadDialog;
import com.example.hikerview.ui.view.colorDialog.PromptDialog;
import com.example.hikerview.utils.StringUtil;
import com.example.hikerview.utils.ToastMgr;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 思路就是通过非 Activity 的 Context 来启动一个透明 activity，
 * 然后使用这个 activity 来显示一个 dialog
 */

/**
 * 作者：By hdy
 * 日期：On 2018/11/16
 * 时间：At 10:51
 */

public class GlobalDialogActivity extends AppCompatActivity {
    private static final String TAG = "GlobalDialogActivity";
    private LoadingPopupView loadingPopupView;
    private DownloadDialog downDialog;
    private View bg;

    public static void startLoading(Context context, String title) {
        Intent starter = new Intent(context, GlobalDialogActivity.class);
        //设置启动方式
        starter.putExtra("type", "Loading");
        starter.putExtra("title", title);
        context.startActivity(starter);
    }

    public static void startDetectUrl(Context context, String title, String url) {
        Intent starter = new Intent(context, GlobalDialogActivity.class);
        //设置启动方式
        starter.putExtra("type", "DetectUrl");
        starter.putExtra("url", url);
        starter.putExtra("title", title);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_dialog);
        bg = findView(R.id.bg);
        bg.setOnClickListener(v -> finish());
        String type = getIntent().getStringExtra("type");
        if (StringUtil.isEmpty(type)) {
            finish();
            return;
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            Log.d(TAG, "onCreate: EventBus register");
            EventBus.getDefault().register(this);
        }
        switch (type) {
            case "SearchHisClear":
                new PromptDialog(getContext())
                        .setTitleText("温馨提示")
                        .setContentText("是否清空搜索记录")
                        .setPositiveListener("清空记录", dialog -> {
                            SearchHistroyModel.clearAll(getContext());
                            ToastMgr.shortBottomCenter(getContext(), "已清除搜索记录");
                        }).show();
                break;
            case "Loading":
                String title = getIntent().getStringExtra("title");
                loadingPopupView = new XPopup.Builder(getContext()).asLoading(title);
                loadingPopupView.show();
                break;
            case "DetectUrl":
                bg.setOnClickListener(v -> {

                });
                detectUrl();
                break;
            default:
                break;
        }
    }

    private void detectUrl() {
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");
        downDialog = new DownloadDialog(getContext());
        downDialog.show();
        new Thread(() -> DownloadManager.instance().detectUrl(url, null, title, new DetectListener() {
            @Override
            public void onSuccess(VideoInfo videoInfo) {
                if(isFinishing()){
                    return;
                }
//                Log.d(TAG, "onSuccess: " + JSON.toJSONString(videoInfo));
                DownloadTask downloadTask = new DownloadTask(
                        UUIDUtil.genUUID(), videoInfo.getFileName(),
                        ("player/m3u8".equals(videoInfo.getVideoFormat().getName()) ? "player/m3u8" : "normal"),
                        videoInfo.getVideoFormat().getName(),
                        videoInfo.getUrl(),
                        videoInfo.getSourcePageUrl(),
                        videoInfo.getSourcePageTitle(),
                        videoInfo.getSize());
                DownloadManager.instance().addTask(downloadTask);
                runOnUiThread(() -> {
                    if(isFinishing()){
                        return;
                    }
                    try {
                        downDialog.dismiss();
                        ToastMgr.shortBottomCenter(getContext(), downloadTask.getSourcePageTitle() + "已加入下载队列");
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailed(String msg) {
                if(isFinishing()){
                    return;
                }
                runOnUiThread(() -> {
                    try {
                        ToastMgr.shortBottomCenter(getContext(), msg);
                        downDialog.dismiss();
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String msg) {
                Log.d(TAG, "onProgress: " + progress);
                runOnUiThread(() -> downDialog.setProgress(progress, msg));
            }
        })).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void loadingDismiss(LoadingDismissEvent event) {
        if (loadingPopupView != null) {
            loadingPopupView.dismiss();
        }
        if (StringUtil.isNotEmpty(event.getMsg())) {
            ToastMgr.shortBottomCenter(getContext(), event.getMsg());
        }
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(null);
    }

    /**
     * 查找View
     *
     * @param id   控件的id
     * @param <VT> View类型
     * @return 鬼知道
     */
    protected <VT extends View> VT findView(@IdRes int id) {
        return (VT) findViewById(id);
    }

    protected Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}