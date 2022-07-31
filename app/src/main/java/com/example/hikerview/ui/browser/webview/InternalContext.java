package com.example.hikerview.ui.browser.webview;

import android.content.Context;
import android.content.MutableContextWrapper;

/**
 * 作者：By 15968
 * 日期：On 2020/8/4
 * 时间：At 21:06
 */

public class InternalContext {

    private static InternalContext instance;
    private MutableContextWrapper mutableContext;

    protected static InternalContext getInstance() {
        if (instance == null) {
            instance = new InternalContext();
        }
        return instance;
    }

    protected void setBaseContext(Context context) {
        if (mutableContext == null) {
            mutableContext = new MutableContextWrapper(context);
        }
        mutableContext.setBaseContext(context);
    }

    protected MutableContextWrapper getMutableContext() {
        return mutableContext;
    }

}
