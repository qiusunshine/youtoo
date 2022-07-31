package com.example.hikerview.event.web;

import com.example.hikerview.ui.view.HorizontalWebView;

/**
 * 作者：By 15968
 * 日期：On 2021/8/15
 * 时间：At 21:34
 */

public class OnCreateWindowEvent {
    private HorizontalWebView webView;
    public OnCreateWindowEvent(HorizontalWebView webView) {
        this.webView = webView;
    }

    public HorizontalWebView getWebView() {
        return webView;
    }

    public void setWebView(HorizontalWebView webView) {
        this.webView = webView;
    }
}