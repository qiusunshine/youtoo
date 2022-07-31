package com.example.hikerview.ui.view.popup;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.hikerview.utils.ScreenUtil;
import com.lxj.xpopup.R.style;
import com.lxj.xpopup.util.FuckRomUtils;
import com.lxj.xpopup.util.XPopupUtils;

/**
 * 作者：By 15968
 * 日期：On 2021/7/31
 * 时间：At 22:01
 */

public class FullScreenDialog extends Dialog {
    View contentView;

    public FullScreenDialog(@NonNull Context context) {
        super(context, style._XPopup_TransparentDialog);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getWindow() != null && this.contentView != null) {

            this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            this.getWindow().getDecorView().setPadding(0, 0, 0, 0);
            this.getWindow().setFlags(16777216, 16777216);
            this.getWindow().setSoftInputMode(16);
            if (this.isFuckVIVORoom()) {
                this.getWindow().getDecorView().setTranslationY((float)(-XPopupUtils.getStatusBarHeight()));
                this.getWindow().setLayout(-1, Math.max(ScreenUtil.getAppHeight(this.getContext()), ScreenUtil.getScreenHeight2(this.getContext())));
            } else {
                this.getWindow().setLayout(-1, Math.max(ScreenUtil.getAppHeight(this.getContext()), ScreenUtil.getScreenHeight2(this.getContext())));
            }

            int option = 1280;
            this.getWindow().getDecorView().setSystemUiVisibility(option);
            if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
                this.setWindowFlag(201326592, true);
            }

            if (Build.VERSION.SDK_INT >= 21) {
                this.setWindowFlag(201326592, false);
                this.getWindow().setStatusBarColor(0);
                this.getWindow().addFlags(-2147483648);
            }

            if (Build.VERSION.SDK_INT == 19) {
                this.getWindow().clearFlags(67108864);
            }

            this.autoSetStatusBarMode();
            this.setContentView(this.contentView);
        }
    }

    public boolean isFuckVIVORoom() {
        boolean isXModel = Build.MODEL.contains("X") || Build.MODEL.contains("x");
        return FuckRomUtils.isVivo() && (Build.VERSION.SDK_INT == 26 || Build.VERSION.SDK_INT == 27) && !isXModel;
    }

    public boolean isActivityStatusBarLightMode() {
        if (Build.VERSION.SDK_INT >= 23) {
            View decorView = ((Activity)this.contentView.getContext()).getWindow().getDecorView();
            int vis = decorView.getSystemUiVisibility();
            return (vis & 8192) != 0;
        } else {
            return false;
        }
    }

    public void setWindowFlag(int bits, boolean on) {
        WindowManager.LayoutParams winParams = this.getWindow().getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }

        this.getWindow().setAttributes(winParams);
    }

    public void autoSetStatusBarMode() {
            if (Build.VERSION.SDK_INT >= 23) {
                View decorView = this.getWindow().getDecorView();
                int vis = decorView.getSystemUiVisibility();
                boolean isLightMode = this.isActivityStatusBarLightMode();
                if (isLightMode) {
                    vis |= 8192;
                } else {
                    vis &= -8193;
                }

                decorView.setSystemUiVisibility(vis);
            }
    }

    public void hideNavigationBar() {
        ViewGroup decorView = (ViewGroup)this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | 6914);
    }

    private String getResNameById(int id) {
        try {
            return this.getContext().getResources().getResourceEntryName(id);
        } catch (Exception var3) {
            return "";
        }
    }

    public FullScreenDialog setContent(View view) {
        if (view.getParent() != null) {
            ((ViewGroup)view.getParent()).removeView(view);
        }

        this.contentView = view;
        return this;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.isFuckVIVORoom()) {
            event.setLocation(event.getX(), event.getY() + (float) XPopupUtils.getStatusBarHeight());
        }

        return super.dispatchTouchEvent(event);
    }

    public void passClick(MotionEvent event) {
        if (this.contentView != null && this.contentView.getContext() instanceof Activity) {
            ((Activity)this.contentView.getContext()).dispatchTouchEvent(event);
        }

    }
}