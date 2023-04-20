package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.ui.setting.model.SettingConfig;
import com.example.hikerview.ui.view.DrawableTextView;
import com.example.hikerview.utils.PreferenceMgr;
import com.lxj.xpopup.core.BottomPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class BrowserMenuPopup extends BottomPopupView {

    private OnItemClickListener onItemClickListener;
    private int greasyForkJsCount;
    private DrawableTextView menuBookmarkAdd, menuPure, menuImageMode;

    public BrowserMenuPopup(@NonNull Context context) {
        super(context);
    }

    public BrowserMenuPopup(@NonNull Activity activity, OnItemClickListener onItemClickListener) {
        super(activity);
        this.onItemClickListener = onItemClickListener;
    }

    public BrowserMenuPopup withGreasyForkMenu(int greasyForkJsCount) {
        this.greasyForkJsCount = greasyForkJsCount;
        return this;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_browser_menu_popup;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();
        DrawableTextView menuBookmark = findViewById(R.id.menuBookmark);
        bindItemClickListener(menuBookmark);
        menuBookmarkAdd = findViewById(R.id.menuBookmarkAdd);
        bindItemClickListener(menuBookmarkAdd);
        DrawableTextView menuHistory = findViewById(R.id.menuHistory);
        bindItemClickListener(menuHistory);
        DrawableTextView menuDownload = findViewById(R.id.menuDownload);
        bindItemClickListener(menuDownload);
        DrawableTextView menuPlugin = findViewById(R.id.menuPlugin);
        if (greasyForkJsCount > 0) {
            menuPlugin.setText("插件(" + greasyForkJsCount + ")");
        } else {
            menuPlugin.setText("插件");
        }
        bindItemClickListener(menuPlugin);
        DrawableTextView menuXiuTan = findViewById(R.id.menuXiuTan);
        bindItemClickListener(menuXiuTan);
        DrawableTextView menuUa = findViewById(R.id.menuUa);
        bindItemClickListener(menuUa);
        DrawableTextView menuUrls = findViewById(R.id.menuUrls);
        bindItemClickListener(menuUrls);
        DrawableTextView menuRefresh = findViewById(R.id.menuRefresh);
        bindItemClickListener(menuRefresh);
        DrawableTextView menuTools = findViewById(R.id.menuTools);
        bindItemClickListener(menuTools);
        menuImageMode = findViewById(R.id.menuImageMode);
        bindItemClickListener(menuImageMode);
        menuPure = findViewById(R.id.menuPure);
        bindItemClickListener(menuPure);
        DrawableTextView menuFullscreen = findViewById(R.id.menuFullscreen);
        bindItemClickListener(menuFullscreen);
        DrawableTextView menuMarkAd = findViewById(R.id.menuMarkAd);
        bindItemClickListener(menuMarkAd);
        DrawableTextView menuConfig = findViewById(R.id.menuConfig);
        bindItemClickListener(menuConfig);

        findViewById(R.id.menuExit).setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick("退出");
                dismiss();
            }
        });
        findViewById(R.id.menuSetting).setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick("设置");
                dismiss();
            }
        });
        findViewById(R.id.menuFold).setOnClickListener(v -> dismiss());

        if (menuPure != null) {
            menuPure.setText(SettingConfig.noWebHistory ? "关闭无痕" : "无痕模式");
        }
        if (menuImageMode != null) {
            boolean blockImg = PreferenceMgr.getBoolean(getContext(), "blockImg", false);
            menuImageMode.setText(blockImg ? "关闭无图" : "无图模式");
        }
    }

    private void bindItemClickListener(DrawableTextView view) {
        view.setOnClickListener(v -> {
            if (onItemClickListener != null && v instanceof DrawableTextView) {
                DrawableTextView drawableTextView = (DrawableTextView) v;
                String t = drawableTextView.getTextView().getText().toString();
                onItemClickListener.onClick(t);
                dismiss();
            }
        });
    }

    public interface OnItemClickListener {
        void onClick(String text);
    }

    public void notifyInBookmarkItem() {
        if (menuBookmarkAdd != null) {
            menuBookmarkAdd.setText("移除书签");
        }
    }
}
