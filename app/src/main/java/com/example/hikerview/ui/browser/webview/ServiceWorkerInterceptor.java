package com.example.hikerview.ui.browser.webview;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.Nullable;

/**
 * 作者：By 15968
 * 日期：On 2022/10/16
 * 时间：At 1:07
 */

public interface ServiceWorkerInterceptor {
    @Nullable
    WebResourceResponse shouldInterceptRequest(WebResourceRequest request);
}
