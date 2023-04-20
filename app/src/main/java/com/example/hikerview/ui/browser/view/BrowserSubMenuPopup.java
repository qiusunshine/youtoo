package com.example.hikerview.ui.browser.view;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.hikerview.R;
import com.example.hikerview.ui.view.DrawableTextView;
import com.lxj.xpopup.core.BottomPopupView;

/**
 * 作者：By 15968
 * 日期：On 2020/3/23
 * 时间：At 21:08
 */
public class BrowserSubMenuPopup extends BottomPopupView {

    private OnItemClickListener onItemClickListener;

    public BrowserSubMenuPopup(@NonNull Context context) {
        super(context);
    }

    public BrowserSubMenuPopup(@NonNull Activity activity, OnItemClickListener onItemClickListener) {
        super(activity);
        this.onItemClickListener = onItemClickListener;
    }

    // 返回自定义弹窗的布局
    @Override
    protected int getImplLayoutId() {
        return R.layout.view_browser_sub_menu_popup;
    }

    // 执行初始化操作，比如：findView，设置点击，或者任何你弹窗内的业务逻辑
    @Override
    protected void onCreate() {
        super.onCreate();

        DrawableTextView menuFind = findViewById(R.id.menuFind);
        bindItemClickListener(menuFind);
        DrawableTextView menuDev = findViewById(R.id.menuDev);
        bindItemClickListener(menuDev);
        DrawableTextView menuTranslate = findViewById(R.id.menuTranslate);
        bindItemClickListener(menuTranslate);
        DrawableTextView menuSave = findViewById(R.id.menuSave);
        bindItemClickListener(menuSave);
        DrawableTextView menuOffline = findViewById(R.id.menuOffline);
        bindItemClickListener(menuOffline);
        DrawableTextView menuCode = findViewById(R.id.menuCode);
        bindItemClickListener(menuCode);
        DrawableTextView menuEyeTheme = findViewById(R.id.menuEyeTheme);
        bindItemClickListener(menuEyeTheme);
        DrawableTextView menuMore = findViewById(R.id.menuMore);
        bindItemClickListener(menuMore);
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
}
