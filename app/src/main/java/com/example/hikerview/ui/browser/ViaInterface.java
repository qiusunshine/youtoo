package com.example.hikerview.ui.browser;

import android.webkit.JavascriptInterface;

/**
 * 作者：By 15968
 * 日期：On 2019/9/29
 * 时间：At 22:54
 */
public class ViaInterface {
    private ViaBridgeListener bridgeListener;

    public ViaInterface(ViaBridgeListener bridgeListener) {
        this.bridgeListener = bridgeListener;
    }

    @JavascriptInterface
    public void addon(final String s) {
        this.bridgeListener.addon(s);
    }

    public interface ViaBridgeListener {
        void addon(final String s);
    }
}
