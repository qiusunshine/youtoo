package com.example.hikerview.ui.browser.view;

import android.view.View;
import android.webkit.WebView;

import com.example.hikerview.ui.base.BaseActivity;

/**
 * 作者：By 15968
 * 日期：On 2020/4/4
 * 时间：At 16:01
 */
public abstract class BaseWebViewActivity extends BaseActivity {
    public abstract WebView getWebView();

    public abstract View getSnackBarBg();

    public abstract boolean isOnPause();
}
